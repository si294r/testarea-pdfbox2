package mkl.testarea.pdfbox2.meta;

import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.Test;

/**
 * @author mkl
 */
public class OpenFile
{
    /**
     * <a href="http://stackoverflow.com/questions/39341760/code-stuck-while-loading-password-protected-pdf-using-pdfbox">
     * Code stuck while loading password protected pdf using pdfbox
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/s/d5ngzsw4a3chlbm/test.pdf?dl=0">
     * test.pdf
     * </a>
     * <p>
     * The issue cannot be reproduced, the provided PDF can be read without any code getting stuck.
     * </p>
     */
    @Test
    public void testOpenTestUser6722137() throws IOException
    {
        try ( InputStream resource = getClass().getResourceAsStream("test.pdf") )
        {
            String password = "test";
            PDDocument pdfdocument = PDDocument.load(resource, password);
            System.out.printf("Producer of User6722137's test.pdf: %s\n", pdfdocument.getDocumentInformation().getProducer());
        }
    }

}
