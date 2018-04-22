package mkl.testarea.pdfbox2.sign;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDSignatureField;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cms.CMSAbsentContent;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.DefaultSignedAttributeTableGenerator;
import org.bouncycastle.cms.SignerInfoGeneratorBuilder;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;
import org.bouncycastle.util.Store;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class CreateSignature
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "sign");

    public static final String KEYSTORE = "keystores/demo-rsa2048.ks"; 
    public static final char[] PASSWORD = "demo-rsa2048".toCharArray(); 

    public static KeyStore ks = null;
    public static PrivateKey pk = null;
    public static Certificate[] chain = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();

        BouncyCastleProvider bcp = new BouncyCastleProvider();
        Security.insertProviderAt(bcp, 1);

        ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(new FileInputStream(KEYSTORE), PASSWORD);
        String alias = (String) ks.aliases().nextElement();
        pk = (PrivateKey) ks.getKey(alias, PASSWORD);
        chain = ks.getCertificateChain(alias);
    }

    /**
     * <a href="http://stackoverflow.com/questions/41767351/create-pkcs7-signature-from-file-digest">
     * Create pkcs7 signature from file digest
     * </a>
     * <p>
     * This test uses the OP's own <code>sign</code> method: {@link #signBySnox(InputStream)}.
     * There are small errors in it, so the result is rejected by verification. These errors
     * are corrected in {@link #signWithSeparatedHashing(InputStream)} which is tested in
     * {@link #testSignWithSeparatedHashing()}.
     * </p>
     */
    @Test
    public void testSignWithSeparatedHashingLikeSnox() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("test.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "testSignedLikeSnox.pdf"));
                PDDocument pdDocument = PDDocument.load(resource)   )
        {
            sign(pdDocument, result, data -> signBySnox(data));
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/41767351/create-pkcs7-signature-from-file-digest">
     * Create pkcs7 signature from file digest
     * </a>
     * <p>
     * This test uses a fixed version of the OP's <code>sign</code> method:
     * {@link #signWithSeparatedHashing(InputStream)}. Here the errors from
     * {@link #signBySnox(InputStream)} are corrected, so the result is not
     * rejected by verification anymore.
     * </p>
     */
    @Test
    public void testSignWithSeparatedHashing() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("test.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "testSignedWithSeparatedHashing.pdf"));
                PDDocument pdDocument = PDDocument.load(resource)   )
        {
            sign(pdDocument, result, data -> signWithSeparatedHashing(data));
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/41767351/create-pkcs7-signature-from-file-digest">
     * Create pkcs7 signature from file digest
     * </a>
     * <p>
     * A minimal signing frame work merely requiring a {@link SignatureInterface}
     * instance.
     * </p>
     */
    void sign(PDDocument document, OutputStream output, SignatureInterface signatureInterface) throws IOException
    {
        PDSignature signature = new PDSignature();
        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName("Example User");
        signature.setLocation("Los Angeles, CA");
        signature.setReason("Testing");
        signature.setSignDate(Calendar.getInstance());
        document.addSignature(signature);
        ExternalSigningSupport externalSigning =
                document.saveIncrementalForExternalSigning(output);
        // invoke external signature service
        byte[] cmsSignature = signatureInterface.sign(externalSigning.getContent());
        // set signature bytes received from the service
        externalSigning.setSignature(cmsSignature);
    }

    /**
     * <a href="http://stackoverflow.com/questions/41767351/create-pkcs7-signature-from-file-digest">
     * Create pkcs7 signature from file digest
     * </a>
     * <p>
     * The OP's own <code>sign</code> method which has some errors. These
     * errors are fixed in {@link #signWithSeparatedHashing(InputStream)}.
     * </p>
     */
    public byte[] signBySnox(InputStream content) throws IOException {
        // testSHA1WithRSAAndAttributeTable
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1", "BC");
            List<Certificate> certList = new ArrayList<Certificate>();
            CMSTypedData msg = new CMSProcessableByteArray(IOUtils.toByteArray(content));

            certList.addAll(Arrays.asList(chain));

            Store certs = new JcaCertStore(certList);

            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();

            Attribute attr = new Attribute(CMSAttributes.messageDigest,
                    new DERSet(new DEROctetString(md.digest(IOUtils.toByteArray(content)))));

            ASN1EncodableVector v = new ASN1EncodableVector();

            v.add(attr);

            SignerInfoGeneratorBuilder builder = new SignerInfoGeneratorBuilder(new BcDigestCalculatorProvider())
                    .setSignedAttributeGenerator(new DefaultSignedAttributeTableGenerator(new AttributeTable(v)));

            AlgorithmIdentifier sha1withRSA = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA1withRSA");

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(chain[0].getEncoded());
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);

            gen.addSignerInfoGenerator(builder.build(
                    new BcRSAContentSignerBuilder(sha1withRSA,
                            new DefaultDigestAlgorithmIdentifierFinder().find(sha1withRSA))
                                    .build(PrivateKeyFactory.createKey(pk.getEncoded())),
                    new JcaX509CertificateHolder(cert)));

            gen.addCertificates(certs);

            CMSSignedData s = gen.generate(new CMSAbsentContent(), false);
            return new CMSSignedData(msg, s.getEncoded()).getEncoded();

        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e);
        }
    }

    /**
     * <a href="http://stackoverflow.com/questions/41767351/create-pkcs7-signature-from-file-digest">
     * Create pkcs7 signature from file digest
     * </a>
     * <p>
     * The OP's <code>sign</code> method after fixing some errors. The
     * OP's original method is {@link #signBySnox(InputStream)}. The
     * errors were
     * </p>
     * <ul>
     * <li>multiple attempts at reading the {@link InputStream} parameter;
     * <li>convoluted creation of final CMS container.
     * </ul>
     * <p>
     * Additionally this method uses SHA256 instead of SHA-1.
     * </p>
     */
    public byte[] signWithSeparatedHashing(InputStream content) throws IOException
    {
        try
        {
            // Digest generation step
            MessageDigest md = MessageDigest.getInstance("SHA256", "BC");
            byte[] digest = md.digest(IOUtils.toByteArray(content));

            // Separate signature container creation step
            List<Certificate> certList = Arrays.asList(chain);
            JcaCertStore certs = new JcaCertStore(certList);

            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();

            Attribute attr = new Attribute(CMSAttributes.messageDigest,
                    new DERSet(new DEROctetString(digest)));

            ASN1EncodableVector v = new ASN1EncodableVector();

            v.add(attr);

            SignerInfoGeneratorBuilder builder = new SignerInfoGeneratorBuilder(new BcDigestCalculatorProvider())
                    .setSignedAttributeGenerator(new DefaultSignedAttributeTableGenerator(new AttributeTable(v)));

            AlgorithmIdentifier sha256withRSA = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256withRSA");

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(chain[0].getEncoded());
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);

            gen.addSignerInfoGenerator(builder.build(
                    new BcRSAContentSignerBuilder(sha256withRSA,
                            new DefaultDigestAlgorithmIdentifierFinder().find(sha256withRSA))
                                    .build(PrivateKeyFactory.createKey(pk.getEncoded())),
                    new JcaX509CertificateHolder(cert)));

            gen.addCertificates(certs);

            CMSSignedData s = gen.generate(new CMSAbsentContent(), false);
            return s.getEncoded();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new IOException(e);
        }
    }

    /**
     * <a href="https://stackoverflow.com/questions/49894319/lock-dictionary-in-signature-field-is-the-reason-of-broken-signature-after-sig">
     * “Lock” dictionary in signature field is the reason of broken signature after signing
     * </a>
     * <p>
     * This test shows how to properly sign signatures in a field with a
     * signature Lock dictionary. In particular important is the addition
     * of FieldMDP transform data to the signature value as here is done
     * in {@link #signExistingFieldWithLock(PDDocument, OutputStream, SignatureInterface)}.
     * </p>
     */
    @Test
    public void testSignWithLocking() throws IOException
    {
        try (   InputStream resource = getClass().getResourceAsStream("test.pdf");
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "testFieldWithLocking.pdf"));
                PDDocument pdDocument = PDDocument.load(resource)   )
        {
            PDAcroForm acroForm = pdDocument.getDocumentCatalog().getAcroForm();
            if (acroForm == null)
            {
                acroForm = new PDAcroForm(pdDocument);
                pdDocument.getDocumentCatalog().setAcroForm(acroForm);
            }
            PDSignatureField signatureField = new PDSignatureField(acroForm);
            acroForm.getFields().add(signatureField);
            signatureField.getWidgets().get(0).setPage(pdDocument.getPage(0));
            pdDocument.getPage(0).getAnnotations().add(signatureField.getWidgets().get(0));
            signatureField.getWidgets().get(0).setRectangle(new PDRectangle(100, 600, 300, 200));
            setLock(signatureField, acroForm);
            pdDocument.save(result);
        }

        try (   InputStream resource = new FileInputStream(new File(RESULT_FOLDER, "testFieldWithLocking.pdf"));
                OutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "testSignedWithLocking.pdf"));
                PDDocument pdDocument = PDDocument.load(resource)   )
        {
            signExistingFieldWithLock(pdDocument, result, data -> signWithSeparatedHashing(data));
        }
}

    /**
     * <p>
     * A minimal signing frame work merely requiring a {@link SignatureInterface}
     * instance signing an existing field.
     * </p>
     * @see #testSignWithLocking()
     */
    void signExistingFieldWithLock(PDDocument document, OutputStream output, SignatureInterface signatureInterface) throws IOException
    {
        PDSignatureField signatureField = document.getSignatureFields().get(0);
        PDSignature signature = new PDSignature();
        signatureField.setValue(signature);

        COSBase lock = signatureField.getCOSObject().getDictionaryObject(COS_NAME_LOCK);
        if (lock instanceof COSDictionary)
        {
            COSDictionary lockDict = (COSDictionary) lock;
            COSDictionary transformParams = new COSDictionary(lockDict);
            transformParams.setItem(COSName.TYPE, COSName.getPDFName("TransformParams"));
            transformParams.setItem(COSName.V, COSName.getPDFName("1.2"));
            transformParams.setDirect(true);
            COSDictionary sigRef = new COSDictionary();
            sigRef.setItem(COSName.TYPE, COSName.getPDFName("SigRef"));
            sigRef.setItem(COSName.getPDFName("TransformParams"), transformParams);
            sigRef.setItem(COSName.getPDFName("TransformMethod"), COSName.getPDFName("FieldMDP"));
            sigRef.setItem(COSName.getPDFName("Data"), document.getDocumentCatalog());
            sigRef.setDirect(true);
            COSArray referenceArray = new COSArray();
            referenceArray.add(sigRef);
            signature.getCOSObject().setItem(COSName.getPDFName("Reference"), referenceArray);
        }

        signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
        signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
        signature.setName("blablabla");
        signature.setLocation("blablabla");
        signature.setReason("blablabla");
        signature.setSignDate(Calendar.getInstance());
        document.addSignature(signature);
        ExternalSigningSupport externalSigning =
                document.saveIncrementalForExternalSigning(output);
        // invoke external signature service
        byte[] cmsSignature = signatureInterface.sign(externalSigning.getContent());
        // set signature bytes received from the service
        externalSigning.setSignature(cmsSignature);
    }

    /**
     * <a href="https://stackoverflow.com/questions/49894319/lock-dictionary-in-signature-field-is-the-reason-of-broken-signature-after-sig">
     * “Lock” dictionary in signature field is the reason of broken signature after signing
     * </a>
     * <p>
     * This code originally was in the OP's SigningUtils class.
     * </p>
     */
    public static void setLock(PDSignatureField pdSignatureField, PDAcroForm acroForm) {
        COSDictionary lockDict = new COSDictionary();
        lockDict.setItem(COS_NAME_ACTION, COS_NAME_ALL);
        lockDict.setItem(COSName.TYPE, COS_NAME_SIG_FIELD_LOCK);
        pdSignatureField.getCOSObject().setItem(COS_NAME_LOCK, lockDict);
    }

    public static final COSName COS_NAME_LOCK = COSName.getPDFName("Lock");
    public static final COSName COS_NAME_ACTION = COSName.getPDFName("Action");
    public static final COSName COS_NAME_ALL = COSName.getPDFName("All");
    public static final COSName COS_NAME_SIG_FIELD_LOCK = COSName.getPDFName("SigFieldLock");
}
