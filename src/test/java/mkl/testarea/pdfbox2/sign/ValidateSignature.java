package mkl.testarea.pdfbox2.sign;

import java.io.ByteArrayInputStream;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;

/**
 * @author mkl
 */
public class ValidateSignature
{
    /**
     * <a href="http://stackoverflow.com/questions/41116833/pdf-signature-validation">
     * PDF Signature Validation
     * </a>
     * <br/>
     * <a href="https://drive.google.com/file/d/0BzEmZ9pRWLhPOUJSYUdlRjg2eEU/view?usp=sharing">
     * SignatureVlidationTest.pdf
     * </a>
     * <p>
     * The code completely ignores the <b>SubFilter</b> of the signature. It is appropriate
     * for signatures with <b>SubFilter</b> values <b>adbe.pkcs7.detached</b> and
     * <b>ETSI.CAdES.detached</b> but will fail for signatures with <b>SubFilter</b> values
     * <b>adbe.pkcs7.sha1</b> and <b>adbe.x509.rsa.sha1</b>.
     * </p>
     * <p>
     * The example document has been signed with a signatures with <b>SubFilter</b> value
     * <b>adbe.pkcs7.sha1</b>.
     * </p>
     */
    @Test
    public void testValidateSignatureVlidationTest() throws Exception
    {
        byte[] pdfByte;
        PDDocument pdfDoc = null;
        SignerInformationVerifier verifier = null;
        try
        {
            pdfByte = IOUtils.toByteArray( this.getClass().getResourceAsStream( "SignatureVlidationTest.pdf" ) );
            pdfDoc = PDDocument.load( new ByteArrayInputStream( pdfByte ));
            PDSignature signature = pdfDoc.getSignatureDictionaries().get( 0 );

            byte[] signatureAsBytes = signature.getContents( pdfByte );
            byte[] signedContentAsBytes = signature.getSignedContent( pdfByte );
            CMSSignedData cms = new CMSSignedData( new CMSProcessableByteArray( signedContentAsBytes ), signatureAsBytes);
            SignerInformation signerInfo = (SignerInformation)cms.getSignerInfos().getSigners().iterator().next();
            X509CertificateHolder cert = (X509CertificateHolder)cms.getCertificates().getMatches( signerInfo.getSID() ).iterator().next();
            verifier = new JcaSimpleSignerInfoVerifierBuilder( ).setProvider( new BouncyCastleProvider() ).build( cert );

            // result if false
            boolean verifyRt = signerInfo.verify( verifier );
            System.out.println("Verify result: " + verifyRt);
        }
        finally
        {
            if( pdfDoc != null )
            {
                pdfDoc.close();
            }
        }

    }
}
