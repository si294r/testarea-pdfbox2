package mkl.testarea.pdfbox2.merge;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDDocumentOutline;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem;
import org.apache.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author mkl
 */
public class MergeDocuments
{
    final static File RESULT_FOLDER = new File("target/test-outputs", "merge");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        RESULT_FOLDER.mkdirs();
    }

    /**
     * <a href="http://stackoverflow.com/questions/42283777/pdfbox-writing-compressed-object-streams">
     * pdfbox writing compressed object streams
     * </a>
     * <br/>
     * <a href="https://www.dropbox.com/sh/elbjegfykxux6wf/AAC8SMU6-7_sAPS7yqgZkDn0a?dl=0">
     * "00002 - Distribuição - dia 10.11.2016.pdf",
     * "00041 - Digitação de Documentos - dia 10.11.2016.pdf",
     * "00041 - Digitação de Documentos - dia 10.11.2016_00042 - Citação para Processo Eletrônico - dia 10.11.2016.pdf" 
     * </a>
     * <p>
     * Given the files the OP shared one cannot reproduce the file size increment
     * claimed by the OP to be caused by `PDFMergerUtility`. If using a sufficiently
     * filled list argument to his own method {@link #criaMarcador(PDDocument, int, List)},
     * though, one can create an arbitrary large result file.
     * </p>
     */
    @Test
    public void testMergeLikeArthurMenezes() throws IOException
    {
        try (   InputStream resource1 = getClass().getResourceAsStream("00002 - Distribuição - dia 10.11.2016.pdf");
                InputStream resource2 = getClass().getResourceAsStream("00041 - Digitação de Documentos - dia 10.11.2016.pdf");
                FileOutputStream result = new FileOutputStream(new File(RESULT_FOLDER, "MergeArthurMenezes.pdf")) )
        {
            List<String> marcadores = Collections.nCopies(2000, "Test");
//            List<String> marcadores = Collections.emptyList();
            concatena(resource1, resource2, result, marcadores);
        }
    }

    public void concatena(InputStream anterior, InputStream novo, OutputStream saida, List<String> marcadores)
            throws IOException
    {
        PDFMergerUtility pdfMerger = new PDFMergerUtility();
        pdfMerger.setDestinationStream(saida);
        PDDocument dest;
        PDDocument src;
        MemoryUsageSetting setupMainMemoryOnly = MemoryUsageSetting.setupMainMemoryOnly();
        if (anterior != null)
        {
            dest = PDDocument.load(anterior, setupMainMemoryOnly);
            src = PDDocument.load(novo, setupMainMemoryOnly);
        }
        else
        {
            dest = PDDocument.load(novo, setupMainMemoryOnly);
            src = new PDDocument();
        }
        int totalPages = dest.getNumberOfPages();
        pdfMerger.appendDocument(dest, src);
        criaMarcador(dest, totalPages, marcadores);
        saida = pdfMerger.getDestinationStream();
        dest.save(saida);
        dest.close();
        src.close();
    }

    private void criaMarcador(PDDocument src, int numPaginas, List<String> marcadores) {
        if (marcadores != null && !marcadores.isEmpty()) {
            PDDocumentOutline documentOutline = src.getDocumentCatalog().getDocumentOutline();          
            if (documentOutline == null) {
                documentOutline = new PDDocumentOutline();
            }
            PDPage page;
            if (src.getNumberOfPages() == numPaginas) {
                page = src.getPage(0);
            } else {
                page = src.getPage(numPaginas);
            }
            PDOutlineItem bookmark = null;
            PDOutlineItem pai = null;
            String etiquetaAnterior = null;
            for (String etiqueta : marcadores) {                
                bookmark = bookmark(pai != null ? pai : documentOutline, etiqueta);
                if (bookmark == null) {
                    if (etiquetaAnterior != null && !etiquetaAnterior.equals(etiqueta) && pai == null) {
                        pai = bookmark(documentOutline, etiquetaAnterior);
                    }
                    bookmark = new PDOutlineItem();
                    bookmark.setTitle(etiqueta);
                    if (marcadores.indexOf(etiqueta) == marcadores.size() - 1) {
                        bookmark.setDestination(page);
                    }
                    if (pai != null) {
                        pai.addLast(bookmark);
                        pai.openNode();
                    } else {
                        documentOutline.addLast(bookmark);
                    }
                } else {
                    pai = bookmark;
                }
                etiquetaAnterior = etiqueta;
            }   
            src.getDocumentCatalog().setDocumentOutline(documentOutline);           
        }       
    }

    private PDOutlineItem bookmark(PDOutlineNode outline, String etiqueta) {             
        PDOutlineItem current = outline.getFirstChild();
        while (current != null) {
            if (current.getTitle().equals(etiqueta)) {
                return current;
            }
            bookmark(current, etiqueta);
            current = current.getNextSibling();
        }
        return current;
    }
}
