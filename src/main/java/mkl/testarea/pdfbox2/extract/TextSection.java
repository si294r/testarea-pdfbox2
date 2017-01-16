package mkl.testarea.pdfbox2.extract;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.text.TextPosition;

/**
 * <a href="http://stackoverflow.com/questions/41654518/how-to-read-pdf-departmentsheader-abstract-refrences-with-pdfbox">
 * How to read PDF departments(header,abstract,refrences) With PDFBox?
 * </a>
 * <br/>
 * <a href="http://merlot.usc.edu/csac-f06/papers/Wang05a.pdf">
 * Wang05a.pdf
 * </a>
 * <p>
 * This class represents a section in the context of extraction of semantic text sections which are recognizable
 * by their characteristics of each line alone, e.g. the sample document supplied by the OP.
 * </p>
 * 
 * @author mkl
 */
public class TextSection
{
    public TextSection(TextSectionDefinition definition, List<List<TextPosition>> header, List<List<List<TextPosition>>> body)
    {
        this.definition = definition;
        this.header = new ArrayList<>(header);
        this.body = new ArrayList<>(body);
    }

    @Override
    public String toString()
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(definition.name).append(": ");
        if (!header.isEmpty())
            stringBuilder.append(toString(header));
        stringBuilder.append('\n');
        for (List<List<TextPosition>> bodyLine : body)
        {
            stringBuilder.append("    ").append(toString(bodyLine)).append('\n');
        }
        return stringBuilder.toString();
    }

    String toString(List<List<TextPosition>> words)
    {
        StringBuilder stringBuilder = new StringBuilder();
        boolean first = true;
        for (List<TextPosition> word : words)
        {
            if (first)
                first = false;
            else
                stringBuilder.append(' ');
            for (TextPosition textPosition : word)
            {
                stringBuilder.append(textPosition.getUnicode());
            }
        }
        // cf. http://stackoverflow.com/a/7171932/1729265
        return Normalizer.normalize(stringBuilder, Form.NFKC);
    }

    final TextSectionDefinition definition;
    final List<List<TextPosition>> header;
    final List<List<List<TextPosition>>> body;
}
