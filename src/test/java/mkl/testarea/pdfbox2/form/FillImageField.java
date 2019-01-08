package mkl.testarea.pdfbox2.form;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class FillImageField {
    final static File RESULT_FOLDER = new File("target/test-outputs", "form");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="https://stackoverflow.com/questions/54081053/setting-image-form-field">
     * Setting image form field
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/1balEQf_P6_KPsJGMGUH3jejLVL5wHFds/view?usp=sharing">
     * test_wo_img.pdf
     * </a>
     * <p>
     * This test uses Renat Gatin's {@link AcroFormPopulator} to solve the
     * task of the OP.
     * </p>
     * <p>
     * Setting the <code>populateAndCopy</code> parameter <code>flatten</code>
     * to <code>true</code> also reveals a bug in form flattening of PDFBox. 
     * </p>
     */
    @Test
    public void testFillWithPopulatorTestWoImg() throws IOException {
        AcroFormPopulator abd = new AcroFormPopulator();
        Map<String, String> data = new HashMap<>();
        data.put("test", "src\\test\\resources\\mkl\\testarea\\pdfbox2\\form\\2x2colored.png");

        abd.populateAndCopy("src\\test\\resources\\mkl\\testarea\\pdfbox2\\form\\test_wo_img.pdf",
                new File(RESULT_FOLDER, "test_wo_img-filled.pdf").getAbsolutePath(), data, false);
    }
}
