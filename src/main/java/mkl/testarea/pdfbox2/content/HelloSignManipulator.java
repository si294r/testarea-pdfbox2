package mkl.testarea.pdfbox2.content;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.contentstream.operator.MissingOperandException;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.contentstream.operator.OperatorProcessor;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdfwriter.ContentStreamWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDTransparencyGroup;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.util.Matrix;

import mkl.testarea.pdfbox2.content.HelloSignAnalyzer.HelloSignField;

/**
 * <a href="http://stackoverflow.com/questions/41071142/pdfbox-remove-a-single-field-from-pdf">
 * PDFBox: Remove a single field from PDF
 * </a>
 * <br/>
 * <a href="https://www.dropbox.com/s/oyv1vjyhkmao1t1/input.pdf?dl=0">
 * input.pdf
 * </a>
 * <p>
 * This class can be used to manipulate a document filled by HelloSign (at least
 * it can be used to manipulate the example document and similar ones). It makes
 * use of information dug up by the {@link HelloSignAnalyzer} class.
 * </p>
 * 
 * @author mkl
 */
public class HelloSignManipulator extends PDFTextStripper
{
    public HelloSignManipulator(HelloSignAnalyzer helloSignAnalyzer) throws IOException
    {
        super();
        this.helloSignAnalyzer = helloSignAnalyzer;
        addOperator(new SelectiveDrawObject());
    }

    public void clearFields(Iterable<String> fieldNames) throws IOException
    {
        try
        {
            Map<String, HelloSignField> fieldMap = helloSignAnalyzer.analyze();
            List<HelloSignField> selectedFields = new ArrayList<>();
            for (String fieldName : fieldNames)
            {
                selectedFields.add(fieldMap.get(fieldName));
            }
            fields = selectedFields;

            PDDocument pdDocument = helloSignAnalyzer.pdDocument;
            setStartPage(pdDocument.getNumberOfPages());
            getText(pdDocument);
        }
        finally
        {
            fields = null;
        }
    }

    class SelectiveDrawObject extends OperatorProcessor
    {
        @Override
        public void process(Operator operator, List<COSBase> arguments) throws IOException
        {
            if (arguments.size() < 1)
            {
                throw new MissingOperandException(operator, arguments);
            }
            COSBase base0 = arguments.get(0);
            if (!(base0 instanceof COSName))
            {
                return;
            }
            COSName name = (COSName) base0;

            if (replacement != null || !helloSignAnalyzer.getLastFormName().equals(name.getName()))
            {
                return;
            }

            if (context.getResources().isImageXObject(name))
            {
                throw new IllegalArgumentException("The form xobject to edit turned out to be an image.");
            }
            
            PDXObject xobject = context.getResources().getXObject(name);

            if (xobject instanceof PDTransparencyGroup)
            {
                throw new IllegalArgumentException("The form xobject to edit turned out to be a transparency group.");
            }
            else if (xobject instanceof PDFormXObject)
            {
                PDFormXObject form = (PDFormXObject) xobject;
                PDFormXObject formReplacement = new PDFormXObject(helloSignAnalyzer.pdDocument);
                formReplacement.setBBox(form.getBBox());
                formReplacement.setFormType(form.getFormType());
                formReplacement.setMatrix(form.getMatrix().createAffineTransform());
                formReplacement.setResources(form.getResources());
                OutputStream outputStream = formReplacement.getContentStream().createOutputStream(COSName.FLATE_DECODE);
                replacement = new ContentStreamWriter(outputStream);
                
                context.showForm(form);

                outputStream.close();
                getResources().put(name, formReplacement);
                replacement = null;
            }
        }

        @Override
        public String getName()
        {
            return "Do";
        }
    }

    //
    // PDFTextStripper overrides
    //
    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException
    {
        if (replacement != null)
        {
            boolean copy = true;
            if (TjTJ.contains(operator.getName()))
            {
                Matrix transformation = getTextMatrix().multiply(getGraphicsState().getCurrentTransformationMatrix());
                float xPos = transformation.getTranslateX();
                float yPos = transformation.getTranslateY();
                for (HelloSignField field : fields)
                {
                    if (field.inField(xPos, yPos))
                    {
                        copy = false;
                    }
                }
            }

            if (copy)
            {
                replacement.writeTokens(operands);
                replacement.writeToken(operator);
            }
        }
        super.processOperator(operator, operands);
    }

    //
    // helper methods
    //
    final HelloSignAnalyzer helloSignAnalyzer;
    final Collection<String> TjTJ = Arrays.asList("Tj", "TJ");
    Iterable<HelloSignField> fields;
    ContentStreamWriter replacement = null;
}
