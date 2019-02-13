package mkl.testarea.pdfbox2.extract;

import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.contentstream.operator.MissingOperandException;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorProcessor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColorN;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingColorSpace;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceCMYKColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceGrayColor;
import org.apache.pdfbox.contentstream.operator.color.SetNonStrokingDeviceRGBColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingColorSpace;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceCMYKColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceGrayColor;
import org.apache.pdfbox.contentstream.operator.color.SetStrokingDeviceRGBColor;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSNumber;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.state.PDGraphicsState;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.apache.pdfbox.util.Matrix;
import org.apache.pdfbox.util.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <a href="https://stackoverflow.com/questions/54637141/background-color-is-incorrect-on-first-page-for-some-reason">
 * Background color is incorrect on first page for some reason
 * </a>
 * <p>
 * This is the OP's class skeleton for a text extractor that recognizes
 * text drawn in the same color as its background. The problem is, though,
 * that a bug in PDFBox' {@link org.apache.pdfbox.pdmodel.common.function.PDFunctionType0#eval(float[])}
 * changes its underlying data. But there also is an unrelated bug here
 * which doesn't play a role in the question at hand, though.
 * </p>
 * 
 * @author Dmitry K
 */
public class PdfToTextInfoConverter extends PDFTextStripper {

    private int rotation = 0;

    private float lowerLeftX = 0;

    private float lowerLeftY = 0;

    private PDPage page = null;

    private GeneralPath linePath;

    private Map<GeneralPath, PDColor> filledPaths;

    private Map<TextPosition, PDColor> nonStrokingColors;

    public PdfToTextInfoConverter(PDDocument pddfDoc) throws IOException {
        addOperator(new SetStrokingColorSpace());
        addOperator(new SetNonStrokingColorSpace());
        addOperator(new SetNonStrokingColorN());
        addOperator(new SetStrokingColor());
        addOperator(new SetNonStrokingColor());
        addOperator(new SetStrokingDeviceGrayColor());
        addOperator(new SetNonStrokingDeviceGrayColor());
        addOperator(new SetStrokingDeviceRGBColor());
        addOperator(new SetNonStrokingDeviceRGBColor());
        addOperator(new SetStrokingDeviceCMYKColor());
        addOperator(new SetNonStrokingDeviceCMYKColor());

        addOperator(new AppendRectangleToPath());
        addOperator(new ClipEvenOddRule());
        addOperator(new ClipNonZeroRule());
        addOperator(new ClosePath());
        addOperator(new CurveTo());
        addOperator(new CurveToReplicateFinalPoint());
        addOperator(new CurveToReplicateInitialPoint());
        addOperator(new EndPath());
        addOperator(new FillEvenOddAndStrokePath());
        addOperator(new FillEvenOddRule());
        addOperator(new FillNonZeroAndStrokePath());
        addOperator(new FillNonZeroRule());
        addOperator(new LineTo());
        addOperator(new MoveTo());
        addOperator(new StrokePath());
        document = pddfDoc;
    }

    public void stripPage(int pageNum, int resolution) throws IOException {
        this.setStartPage(pageNum + 1);
        this.setEndPage(pageNum + 1);
        page = document.getPage(pageNum);
        rotation = page.getRotation();
        linePath = new GeneralPath();
        filledPaths = new LinkedHashMap<>();
        nonStrokingColors = new HashMap<>();    
        Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
        writeText(document, dummy); // This call starts the parsing process and calls writeString repeatedly.
    }

    @Override
    public void processPage(PDPage page) throws IOException {
        PDRectangle pageSize = page.getCropBox();

        lowerLeftX = pageSize.getLowerLeftX();
        lowerLeftY = pageSize.getLowerLeftY();

        super.processPage(page);
    }

    private Integer getCharacterBackgroundColor(TextPosition text) {
        Integer fillColorRgb = null;
        try {           
            for (Map.Entry<GeneralPath, PDColor> filledPath : filledPaths.entrySet()) {
                Vector center = getTextPositionCenterPoint(text);
                if (filledPath.getKey().contains(lowerLeftX + center.getX(), lowerLeftY + center.getY())) {
                    fillColorRgb = filledPath.getValue().toRGB();                   
                }
            }
        } catch (IOException e) {
            logger.error("Could not convert color to RGB", e);
        }
        return fillColorRgb;
    }

    private int getCharacterColor(TextPosition text) {
        int colorRgb = 0; // assume it's black even if we could not convert to RGB
        try {
            colorRgb = nonStrokingColors.get(text).toRGB();         
        } catch (IOException e) {
            logger.error("Could not convert color to RGB", e);
        }
        return colorRgb;
    }

    @Override
    protected void processTextPosition(TextPosition text) {
        PDGraphicsState gs = getGraphicsState();
        // check opacity for stroke and fill text 
        if (gs.getAlphaConstant() < Constants.EPSILON && gs.getNonStrokeAlphaConstant() < Constants.EPSILON) {
            return;
        }                       

        Vector center = getTextPositionCenterPoint(text);
        Area area = gs.getCurrentClippingPath();
        if (area == null || area.contains(lowerLeftX + center.getX(), lowerLeftY + center.getY())) {            
            nonStrokingColors.put(text, gs.getNonStrokingColor());
            super.processTextPosition(text);
        }
    }

    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
        for (TextPosition text : textPositions) {           
            Integer characterColor = getCharacterColor(text);
            Integer characterBackgroundColor = getCharacterBackgroundColor(text);
            if ((characterColor != null && characterColor.equals(characterBackgroundColor)) || characterColor == characterBackgroundColor) {
                logger.info(String.format("Color and background coincide for '%s' at %3.2f, %3.2f : %h", text.getUnicode(), text.getX(), text.getY(), characterColor));
            }
        }
    }

    private Vector getTextPositionCenterPoint(TextPosition text) {
        Matrix textMatrix = text.getTextMatrix();
        Vector start = textMatrix.transform(new Vector(0, 0));
        Vector center = null;
        switch (rotation) {
        case 0:
            center = new Vector(start.getX() + text.getWidth()/2, start.getY()); 
            break;
        case 90:
            center = new Vector(start.getX(), start.getY() + text.getWidth()/2);
            break;
        case 180:
            center = new Vector(start.getX() - text.getWidth()/2, start.getY());
            break;
        case 270:
            center = new Vector(start.getX(), start.getY() - text.getWidth()/2);
            break;
        default:
            center = new Vector(start.getX() + text.getWidth()/2, start.getY());
            break;
        }

        return center;
    }

    void addFillPath(PDColor color) {
        filledPaths.put((GeneralPath)linePath.clone(), color);
    }

    public final class AppendRectangleToPath extends OperatorProcessor {
        @Override
        public void process(Operator operator, List<COSBase> operands) throws IOException {
            if (operands.size() < 4) {
                throw new MissingOperandException(operator, operands);
            }
            if (!checkArrayTypesClass(operands, COSNumber.class)) {
                return;
            }
            COSNumber x = (COSNumber) operands.get(0);
            COSNumber y = (COSNumber) operands.get(1);
            COSNumber w = (COSNumber) operands.get(2);
            COSNumber h = (COSNumber) operands.get(3);

            float x1 = x.floatValue();
            float y1 = y.floatValue();

            // create a pair of coordinates for the transformation
            float x2 = w.floatValue() + x1;
            float y2 = h.floatValue() + y1;

            Point2D p0 = context.transformedPoint(x1, y1);
            Point2D p1 = context.transformedPoint(x2, y1);
            Point2D p2 = context.transformedPoint(x2, y2);
            Point2D p3 = context.transformedPoint(x1, y2);

            // to ensure that the path is created in the right direction, we have to create
            // it by combining single lines instead of creating a simple rectangle
            linePath.moveTo((float) p0.getX(), (float) p0.getY());
            linePath.lineTo((float) p1.getX(), (float) p1.getY());
            linePath.lineTo((float) p2.getX(), (float) p2.getY());
            linePath.lineTo((float) p3.getX(), (float) p3.getY());

            // close the subpath instead of adding the last line so that a possible set line
            // cap style isn't taken into account at the "beginning" of the rectangle
            linePath.closePath();
        }

        @Override
        public String getName() {
            return "re";
        }
    }

    public final class StrokePath extends OperatorProcessor {
        @Override
        public void process(Operator operator, List<COSBase> operands) throws IOException {
            linePath.reset();
        }

        @Override
        public String getName() {
            return "S";
        }
    }

    public final class FillEvenOddRule extends OperatorProcessor {
        @Override
        public void process(Operator operator, List<COSBase> operands) throws IOException {
            PDGraphicsState gs = getGraphicsState();
            linePath.setWindingRule(GeneralPath.WIND_EVEN_ODD);
            addFillPath(gs.getNonStrokingColor());
            linePath.reset();
        }

        @Override
        public String getName() {
            return "f*";
        }
    }        

    public class FillNonZeroRule extends OperatorProcessor {
        @Override
        public final void process(Operator operator, List<COSBase> operands) throws IOException {
            PDGraphicsState gs = getGraphicsState();    
            linePath.setWindingRule(GeneralPath.WIND_NON_ZERO);
            addFillPath(gs.getNonStrokingColor());
            linePath.reset();
        }

        @Override
        public String getName() {
            return "f";
        }
    }

    public class LegacyFillNonZeroRule extends FillNonZeroRule {
        @Override
        public String getName() {
            return "F";
        }
    }

    public final class FillEvenOddAndStrokePath extends OperatorProcessor {
        @Override
        public void process(Operator operator, List<COSBase> operands) throws IOException {
            PDGraphicsState gs = getGraphicsState();

            linePath.setWindingRule(GeneralPath.WIND_EVEN_ODD);
            addFillPath(gs.getNonStrokingColor());
            linePath.reset();
        }

        @Override
        public String getName() {
            return "B*";
        }
    }

    public class FillNonZeroAndStrokePath extends OperatorProcessor {
        @Override
        public void process(Operator operator, List<COSBase> operands) throws IOException {
            PDGraphicsState gs = getGraphicsState();

            linePath.setWindingRule(GeneralPath.WIND_NON_ZERO);
            addFillPath(gs.getNonStrokingColor());
            linePath.reset();
        }

        @Override
        public String getName() {
            return "B";
        }
    }

    public final class ClipEvenOddRule extends OperatorProcessor {
        @Override
        public void process(Operator operator, List<COSBase> operands) throws IOException {
            linePath.setWindingRule(GeneralPath.WIND_EVEN_ODD);
            getGraphicsState().intersectClippingPath(linePath);
        }

        @Override
        public String getName() {
            return "W*";
        }
    }

    public class ClipNonZeroRule extends OperatorProcessor {
        @Override
        public void process(Operator operator, List<COSBase> operands) throws IOException {
            linePath.setWindingRule(GeneralPath.WIND_NON_ZERO);
            getGraphicsState().intersectClippingPath(linePath);
        }

        @Override
        public String getName() {
            return "W";
        }
    }

    public final class MoveTo extends OperatorProcessor {
        @Override
        public void process(Operator operator, List<COSBase> operands) throws IOException {
            if (operands.size() < 2) {
                throw new MissingOperandException(operator, operands);
            }
            COSBase base0 = operands.get(0);
            if (!(base0 instanceof COSNumber)) {
                return;
            }
            COSBase base1 = operands.get(1);
            if (!(base1 instanceof COSNumber)) {
                return;
            }
            COSNumber x = (COSNumber) base0;
            COSNumber y = (COSNumber) base1;
            Point2D.Float pos = context.transformedPoint(x.floatValue(), y.floatValue());
            linePath.moveTo(pos.x, pos.y);
        }

        @Override
        public String getName() {
            return "m";
        }
    }

    public class LineTo extends OperatorProcessor {
        @Override
        public void process(Operator operator, List<COSBase> operands) throws IOException {
            if (operands.size() < 2) {
                throw new MissingOperandException(operator, operands);
            }
            COSBase base0 = operands.get(0);
            if (!(base0 instanceof COSNumber)) {
                return;
            }
            COSBase base1 = operands.get(1);
            if (!(base1 instanceof COSNumber)) {
                return;
            }
            // append straight line segment from the current point to the point
            COSNumber x = (COSNumber) base0;
            COSNumber y = (COSNumber) base1;

            Point2D.Float pos = context.transformedPoint(x.floatValue(), y.floatValue());

            linePath.lineTo(pos.x, pos.y);
        }

        @Override
        public String getName() {
            return "l";
        }
    }

    public class CurveTo extends OperatorProcessor {
        @Override
        public void process(Operator operator, List<COSBase> operands) throws IOException {
            if (operands.size() < 6) {
                throw new MissingOperandException(operator, operands);
            }
            if (!checkArrayTypesClass(operands, COSNumber.class)) {
                return;
            }
            COSNumber x1 = (COSNumber) operands.get(0);
            COSNumber y1 = (COSNumber) operands.get(1);
            COSNumber x2 = (COSNumber) operands.get(2);
            COSNumber y2 = (COSNumber) operands.get(3);
            COSNumber x3 = (COSNumber) operands.get(4);
            COSNumber y3 = (COSNumber) operands.get(5);

            Point2D.Float point1 = context.transformedPoint(x1.floatValue(), y1.floatValue());
            Point2D.Float point2 = context.transformedPoint(x2.floatValue(), y2.floatValue());
            Point2D.Float point3 = context.transformedPoint(x3.floatValue(), y3.floatValue());

            linePath.curveTo(point1.x, point1.y, point2.x, point2.y, point3.x, point3.y);
        }

        @Override
        public String getName() {
            return "c";
        }
    }

    public final class CurveToReplicateFinalPoint extends OperatorProcessor {
        @Override
        public void process(Operator operator, List<COSBase> operands) throws IOException {
            if (operands.size() < 4) {
                throw new MissingOperandException(operator, operands);
            }
            if (!checkArrayTypesClass(operands, COSNumber.class)) {
                return;
            }
            COSNumber x1 = (COSNumber) operands.get(0);
            COSNumber y1 = (COSNumber) operands.get(1);
            COSNumber x3 = (COSNumber) operands.get(2);
            COSNumber y3 = (COSNumber) operands.get(3);

            Point2D.Float point1 = context.transformedPoint(x1.floatValue(), y1.floatValue());
            Point2D.Float point3 = context.transformedPoint(x3.floatValue(), y3.floatValue());

            linePath.curveTo(point1.x, point1.y, point3.x, point3.y, point3.x, point3.y);
        }

        @Override
        public String getName() {
            return "y";
        }
    }

    public class CurveToReplicateInitialPoint extends OperatorProcessor {
        @Override
        public void process(Operator operator, List<COSBase> operands) throws IOException {
            if (operands.size() < 4) {
                throw new MissingOperandException(operator, operands);
            }
            if (!checkArrayTypesClass(operands, COSNumber.class)) {
                return;
            }
            COSNumber x2 = (COSNumber) operands.get(0);
            COSNumber y2 = (COSNumber) operands.get(1);
            COSNumber x3 = (COSNumber) operands.get(2);
            COSNumber y3 = (COSNumber) operands.get(3);

            Point2D currentPoint = linePath.getCurrentPoint();

            Point2D.Float point2 = context.transformedPoint(x2.floatValue(), y2.floatValue());
            Point2D.Float point3 = context.transformedPoint(x3.floatValue(), y3.floatValue());

            linePath.curveTo((float) currentPoint.getX(), (float) currentPoint.getY(), point2.x, point2.y, point3.x, point3.y);
        }

        @Override
        public String getName() {
            return "v";
        }
    }

    public final class ClosePath extends OperatorProcessor {
        @Override
        public void process(Operator operator, List<COSBase> operands) throws IOException {
            linePath.closePath();
        }

        @Override
        public String getName() {
            return "h";
        }
    }

    public final class EndPath extends OperatorProcessor {
        @Override
        public void process(Operator operator, List<COSBase> operands) throws IOException {
            linePath.reset();
        }

        @Override
        public String getName() {
            return "n";
        }
    }

    Logger logger = LoggerFactory.getLogger(PdfToTextInfoConverter.class);

    interface Constants {
        float EPSILON = 0.01f;
    }
}