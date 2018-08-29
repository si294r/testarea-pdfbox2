package mkl.testarea.pdfbox2.form;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class FillInForm
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/36926060/apache-pdfbox-form-fill-truetype-text-spacing-issue">
     * Apache PDFBox Form Fill TrueType text spacing issue
     * </a>
     * <br>
     * <a href="https://www.dropbox.com/sh/b7ft1k0wfesob8s/AABnrTOgX26JlWCxl85jXns0a/FillFormField.pdf?dl=0">
     * FillFormField.pdf
     * </a>
     * <p>
     * Indeed, the issue can be reproduced, it is due to a combination of two factors:
     * </p>
     * <p>
     * <b>A quirk of PDFBox when writing text</b> - When writing text into a content stream,
     * PDFBox translates each Unicode codepoint into a name and looks up that name in a map
     * generating from the inverted font encoding. For some encodings, though, there are two
     * codes mapping to the name space, and the inverted map maps back to only one of them,
     * in the case at hand the non-breaking variant. As both are expected to be typographically
     * identical, this should not be a problem. But:
     * </p>
     * <p>
     * <b>Non-conformant font in the PDF</b> - The font Impact in the PDF is defined with
     * width 176 for the normal space glyph and 750 for the nonbreaking space glyph. Thus,
     * they typographically differ vehemently.
     * </p>
     */
    @Test
    public void testFillLikeRichardBrown() throws IOException
    {
        try (   InputStream originalStream = getClass().getResourceAsStream("FillFormField.pdf") )
        {
            // load the documents
            PDDocument pdfDocument = PDDocument.load(originalStream);

            // get the document catalog
            PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();

            // as there might not be an AcroForm entry a null check is necessary
            if (acroForm != null)
            {
                PDTextField field = (PDTextField) acroForm.getField( "Title" );
                field.setValue("Low Mileage Beauty Kill");
            }

            // Save and close the filled out form.
            pdfDocument.save(new File(RESULT_FOLDER, "FillFormFieldRichardBrown.pdf"));
            pdfDocument.close();
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/39720305/ufffd-is-not-available-in-this-fonts-encoding-winansiencoding">
     * U+FFFD is not available in this font's encoding: WinAnsiEncoding
     * </a>
     * <p>
     * The issue cannot be reproduced.
     * </p>
     */
    @Test
    public void testFillLikeStDdt() throws IOException
    {
        try (   InputStream originalStream = getClass().getResourceAsStream("FillFormField.pdf") )
        {
            PDDocument pdfDocument = PDDocument.load(originalStream);
            PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();

            if (acroForm != null)
            {
                List<PDField> fields = acroForm.getFields();
                for (PDField field : fields) {
                    switch (field.getPartialName()) {
                        case "Title" /*"devices"*/:
                            field.setValue("Ger�t");
                            field.setReadOnly(true);
                            break;
                    }
                }
                acroForm.flatten(fields, true);
            }

            pdfDocument.save(new File(RESULT_FOLDER, "FillFormFieldStDdt.pdf"));
            pdfDocument.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/49048556/pdfbox-set-field-value-doesnt-work">
     * PDFBox set field value doesn't work
     * </a>
     * <br/>
     * <a href="https://www.inps.it/Nuovoportaleinps/image.aspx?iIDModulo=7712&tipomodulo=1">
     * SR16_ANF_DIP.pdf
     * </a>
     * <p>
     * Indeed, the form field in question is hidden. Thus, one has to un-hide it
     * to make it visible.
     * </p>
     */
    @Test
    public void testFillLikeBarbara() throws IOException
    {
        try (   InputStream originalStream = getClass().getResourceAsStream("SR16_ANF_DIP.pdf") )
        {
            PDDocument pdfDocument = PDDocument.load(originalStream);
            PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();

            if (acroForm != null)
            {
                PDTextField pdfField = (PDTextField) acroForm.getField("info_15a");
                pdfField.getWidgets().get(0).setHidden(false);// <===
                pdfField.setValue("xxxxxx");
            }

            pdfDocument.setAllSecurityToBeRemoved(true);
            COSDictionary dictionary = pdfDocument.getDocumentCatalog().getCOSObject();
            dictionary.removeItem(COSName.PERMS);

            pdfDocument.save(new File(RESULT_FOLDER, "SR16_ANF_DIP-filled.pdf"));
            pdfDocument.close();
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/52059931/pdfbox-setvalue-for-multiple-pdtextfield">
     * PDFBox setValue for multiple PDTextField
     * </a>
     * <br/>
     * <a href="https://ufile.io/z8jzj">
     * testform.pdf
     * </a>
     * <p>
     * Cannot reproduce the issue.
     * </p>
     */
    @Test
    public void testFillLikeJuvi() throws IOException {
        try (   InputStream originalStream = getClass().getResourceAsStream("testform.pdf") ) {
            PDDocument document = PDDocument.load(originalStream);
            PDDocumentCatalog docCatalog = document.getDocumentCatalog();
            PDAcroForm acroForm = docCatalog.getAcroForm();

            PDTextField field = (PDTextField) acroForm.getField("Check1");
            field.setValue("1111");

            PDTextField field2 = (PDTextField) acroForm.getField("Check2");
            field2.setValue("2222");

            PDTextField field3 = (PDTextField) acroForm.getField("HelloWorld");
            field3.setValue("HelloWorld");

            document.save(new File(RESULT_FOLDER, "testform-filled.pdf"));
            document.close();
        }
    }
}
