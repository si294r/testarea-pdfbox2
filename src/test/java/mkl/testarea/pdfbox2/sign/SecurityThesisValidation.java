package mkl.testarea.pdfbox2.sign;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Security;

import org.apache.pdfbox.examples.signature.ShowSignature;
import org.apache.pdfbox.examples.signature.cert.CertificateVerificationException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.tsp.TSPException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * <a href="https://stackoverflow.com/questions/55049270/how-can-i-prevent-universal-signature-forgery-usf-incremental-saving-attack">
 * How can i prevent Universal Signature Forgery (USF) , Incremental Saving Attack (ISA), Signature Wrapping (SWA) in Apache PDFBox
 * </a>
 * <br/>
 * <a href="https://pdf-insecurity.org/download/exploits.tar.gz">
 * exploits.tar.gz
 * </a> containing the test files.
 * <p>
 * This test runs the PDFBox {@link ShowSignature} example for the faux
 * PDF signatures from <a href="https://pdf-insecurity.org/index.html">
 * the PDF insecurity site</a>. In all cases validation either fails due
 * to some exception or at least outputs "Signature does not cover whole
 * document".
 * </p>
 * 
 * @author mkl
 */
@RunWith(Parameterized.class)
public class SecurityThesisValidation {
    final static File RESULT_FOLDER = new File("target/test-outputs", "sign");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        RESULT_FOLDER.mkdirs();
    }

    @Parameters(name = "{index}: Validate {1} - {0}")
    public static Object[][] data()
    {
        return new Object[][]
        {
            // Comment, Document file

            {"Incremental Saving Attack: A", "isa_A.pdf"},
            {"Incremental Saving Attack: B", "isa_B.pdf"},
            {"Incremental Saving Attack: C", "isa_C.pdf"},
            {"Incremental Saving Attack: D", "isa_D.pdf"},
            {"Incremental Saving Attack: E", "isa_E.pdf"},
            {"Incremental Saving Attack: F", "isa_F.pdf"},
            {"Incremental Saving Attack: G", "isa_G.pdf"},
            {"Incremental Saving Attack: H", "isa_H.pdf"},
            {"Incremental Saving Attack: I", "isa_I.pdf"},
            {"Signature Wrapping Attack: A", "siwa_A.pdf"},
            {"Signature Wrapping Attack: B", "siwa_B.pdf"},
            {"Signature Wrapping Attack: C", "siwa_C.pdf"},
            {"Signature Wrapping Attack: D", "siwa_D.pdf"},
            {"Signature Wrapping Attack: E", "siwa_E.pdf"},
            {"Universal Signature Forgery: Empty Contents String", "usf_01_Empty_Contents.pdf"},
            {"Universal Signature Forgery: Contents String with Single Zero Byte", "usf_02_00_Contents.pdf"},
            {"Universal Signature Forgery: Null Contents", "usf_03_Null_Contents.pdf"},
            {"Universal Signature Forgery: No Contents String", "usf_04_No_Contents_Value.pdf"},
            {"Universal Signature Forgery: No Contents Entry", "usf_05_No_Contents.pdf"},
            {"Universal Signature Forgery: Null ByteRange", "usf_12_Null_ByteRange.pdf"},
            {"Universal Signature Forgery: No ByteRange", "usf_14_No_ByteRange.pdf "}

        };
    }

    @Parameter(0)
    public String comment;

    @Parameter(1)
    public String docPath;

    @Test
    public void test() throws IOException, TSPException, CertificateVerificationException, GeneralSecurityException {
        System.out.printf("\n\n%s (%s)\n-----\n", docPath, comment);
        ShowSignature.main(new String[] {"", "src\\test\\resources\\mkl\\testarea\\pdfbox2\\sign\\" + docPath});
    }

}
