package mkl.testarea.pdfbox2.content;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

/**
 * <a href="http://stackoverflow.com/questions/41071142/pdfbox-remove-a-single-field-from-pdf">
 * PDFBox: Remove a single field from PDF
 * </a>
 * <br/>
 * <a href="https://www.dropbox.com/s/oyv1vjyhkmao1t1/input.pdf?dl=0">
 * input.pdf
 * </a>
 * <p>
 * This class can be used to analyze a document filled by HelloSign (at least
 * it can be used to analyze the example document and similar ones). 
 * </p>
 * 
 * @author mkl
 */
public class HelloSignAnalyzer extends PDFTextStripper
{
    public class HelloSignField
    {
        public String getName()
        {
            return name;
        }
        public String getValue()
        {
            return value;
        }
        public float getX()
        {
            return x;
        }
        public float getY()
        {
            return y;
        }
        public float getWidth()
        {
            return width;
        }
        public String getType()
        {
            return type;
        }
        public boolean isOptional()
        {
            return optional;
        }
        public String getSigner()
        {
            return signer;
        }
        public String getDisplay()
        {
            return display;
        }
        public String getLabel()
        {
            return label;
        }
        public float getLastX()
        {
            return lastX;
        }

        String name = null;
        String value = "";
        float x = 0, y = 0, width = 0;
        String type = null;
        boolean optional = false;
        String signer = null;
        String display = null;
        String label = null;

        float lastX = 0;

        @Override
        public String toString()
        {
            return String.format("[Name: '%s'; Value: `%s` Position: %s, %s; Width: %s; Type: '%s'; Optional: %s; Signer: '%s'; Display: '%s', Label: '%s']",
                    name, value, x, y, width, type, optional, signer, display, label);
        }

        void checkForValue(List<TextPosition> textPositions)
        {
            for (TextPosition textPosition : textPositions)
            {
                if (inField(textPosition))
                {
                    float textX = textPosition.getTextMatrix().getTranslateX();
                    if (textX > lastX + textPosition.getWidthOfSpace() / 2 && value.length() > 0)
                        value += " ";
                    value += textPosition.getUnicode();
                    lastX = textX + textPosition.getWidth();
                }
            }
        }

        boolean inField(TextPosition textPosition)
        {
            float yPos = textPosition.getTextMatrix().getTranslateY();
            float xPos = textPosition.getTextMatrix().getTranslateX();

            return inField(xPos, yPos);
        }

        boolean inField(float xPos, float yPos)
        {
            if (yPos < y - 3 || yPos > y + 3)
                return false;

            if (xPos < x - 1 || xPos > x + width + 1)
                return false;

            return true;
        }
    }

    public HelloSignAnalyzer(PDDocument pdDocument) throws IOException
    {
        super();
        this.pdDocument = pdDocument;
    }

    public Map<String, HelloSignField> analyze() throws IOException
    {
        if (!analyzed)
        {
            fields = new HashMap<>();

            setStartPage(pdDocument.getNumberOfPages());
            getText(pdDocument);

            analyzed = true;
        }
        return Collections.unmodifiableMap(fields);
    }

    public String getLastFormName()
    {
        return lastFormName;
    }

    //
    // PDFTextStripper overrides
    //
    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException
    {
        {
            for (HelloSignField field : fields.values())
            {
                field.checkForValue(textPositions);
            }
        }

        int position = -1;
        while ((position = text.indexOf('[', position + 1)) >= 0)
        {
            int endPosition = text.indexOf(']', position);
            if (endPosition < 0)
                continue;
            if (endPosition > position + 1 && text.charAt(position + 1) == '$')
            {
                String fieldName = text.substring(position + 2, endPosition);
                int spacePosition = fieldName.indexOf(' ');
                if (spacePosition >= 0)
                    fieldName = fieldName.substring(0, spacePosition);
                HelloSignField field = getOrCreateField(fieldName);

                TextPosition start = textPositions.get(position);
                field.x = start.getTextMatrix().getTranslateX();
                field.y = start.getTextMatrix().getTranslateY();
                TextPosition end = textPositions.get(endPosition);
                field.width = end.getTextMatrix().getTranslateX() + end.getWidth() - field.x;
            }
            else if (endPosition > position + 5 && "def:$".equals(text.substring(position + 1, position + 6)))
            {
                String definition = text.substring(position + 6, endPosition);
                String[] pieces = definition.split("\\|");
                if (pieces.length == 0)
                    continue;
                HelloSignField field = getOrCreateField(pieces[0]);

                if (pieces.length > 1)
                    field.type = pieces[1];
                if (pieces.length > 2)
                    field.optional = !"req".equals(pieces[2]);
                if (pieces.length > 3)
                    field.signer = pieces[3];
                if (pieces.length > 4)
                    field.display = pieces[4];
                if (pieces.length > 5)
                    field.label = pieces[5];
            }
        }

        super.writeString(text, textPositions);
    }

    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException
    {
        String currentFormName = formName; 
        if (operator != null && "Do".equals(operator.getName()) && operands != null && operands.size() > 0)
        {
            COSBase base0 = operands.get(0);
            if (base0 instanceof COSName)
            {
                formName = ((COSName)base0).getName();
                if (currentFormName == null)
                    lastFormName = formName;
            }
        }
        try
        {
            super.processOperator(operator, operands);
        }
        finally
        {
            formName = currentFormName;
        }
    }

    //
    // helper methods
    //
    HelloSignField getOrCreateField(String name)
    {
        HelloSignField field = fields.get(name);
        if (field == null)
        {
            field = new HelloSignField();
            field.name = name;
            fields.put(name, field);
        }
        return field;
    }

    //
    // inner member variables
    //
    final PDDocument pdDocument;
    boolean analyzed = false;
    Map<String, HelloSignField> fields = null;
    String formName = null;
    String lastFormName = null;
}
