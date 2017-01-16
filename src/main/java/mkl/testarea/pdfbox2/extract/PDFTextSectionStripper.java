package mkl.testarea.pdfbox2.extract;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
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
 * This {@link PDFTextStripper} subclass is the hub of a simple framework for extraction
 * of semantic text sections which are recognizable by their characteristics of each line
 * alone, e.g. the sample document supplied by the OP.
 * </p>
 * 
 * @author mkl
 */
public class PDFTextSectionStripper extends PDFTextStripper
{
    //
    // constructor
    //
    public PDFTextSectionStripper(List<TextSectionDefinition> sectionDefinitions) throws IOException
    {
        super();
        
        this.sectionDefinitions = sectionDefinitions;
    }

    //
    // Section retrieval
    //
    /**
     * @return an unmodifiable list of text sections recognized during {@link #getText(PDDocument)}.
     */
    public List<TextSection> getSections()
    {
        return Collections.unmodifiableList(sections);
    }

    //
    // PDFTextStripper overrides
    //
    @Override
    protected void writeLineSeparator() throws IOException
    {
        super.writeLineSeparator();

        if (!currentLine.isEmpty())
        {
            boolean matched = false;
            if (!(currentHeader.isEmpty() && currentBody.isEmpty()))
            {
                TextSectionDefinition definition = sectionDefinitions.get(currentSectionDefinition);
                switch (definition.multiLine)
                {
                case multiLine:
                    if (definition.matchPredicate.test(currentLine))
                    {
                        currentBody.add(new ArrayList<>(currentLine));
                        matched = true;
                    }
                    break;
                case multiLineHeader:
                case multiLineIntro:
                    boolean followUpMatch = false;
                    for (int i = definition.multiple ? currentSectionDefinition : currentSectionDefinition + 1;
                            i < sectionDefinitions.size(); i++)
                    {
                        TextSectionDefinition followUpDefinition = sectionDefinitions.get(i);
                        if (followUpDefinition.matchPredicate.test(currentLine))
                        {
                            followUpMatch = true;
                            break;
                        }
                    }
                    if (!followUpMatch)
                    {
                        currentBody.add(new ArrayList<>(currentLine));
                        matched = true;
                    }
                    break;
                case singleLine:
                    System.out.println("Internal error: There can be no current header or body as long as the current definition is single line only");
                }

                if (!matched)
                {
                    sections.add(new TextSection(definition, currentHeader, currentBody));
                    currentHeader.clear();
                    currentBody.clear();
                    if (!definition.multiple)
                        currentSectionDefinition++;
                }
            }

            if (!matched)
            {
                while (currentSectionDefinition < sectionDefinitions.size())
                {
                    TextSectionDefinition definition = sectionDefinitions.get(currentSectionDefinition);
                    if (definition.matchPredicate.test(currentLine))
                    {
                        matched = true;
                        switch (definition.multiLine)
                        {
                        case singleLine:
                            sections.add(new TextSection(definition, currentLine, Collections.emptyList()));
                            if (!definition.multiple)
                                currentSectionDefinition++;
                            break;
                        case multiLineHeader:
                            currentHeader.addAll(new ArrayList<>(currentLine));
                            break;
                        case multiLine:
                        case multiLineIntro:
                            currentBody.add(new ArrayList<>(currentLine));
                            break;
                        }
                        break;
                    }

                    currentSectionDefinition++;
                }
            }

            if (!matched)
            {
                System.out.println("Could not match line.");
            }
        }
        currentLine.clear();
    }

    @Override
    protected void endDocument(PDDocument document) throws IOException
    {
        super.endDocument(document);

        if (!(currentHeader.isEmpty() && currentBody.isEmpty()))
        {
            TextSectionDefinition definition = sectionDefinitions.get(currentSectionDefinition);
            sections.add(new TextSection(definition, currentHeader, currentBody));
            currentHeader.clear();
            currentBody.clear();
        }
    }

    @Override
    protected void writeString(String text, List<TextPosition> textPositions) throws IOException
    {
        super.writeString(text, textPositions);

        currentLine.add(textPositions);
    }
    
    //
    // member variables
    //
    final List<TextSectionDefinition> sectionDefinitions;

    int currentSectionDefinition = 0;
    final List<TextSection> sections = new ArrayList<>();
    final List<List<TextPosition>> currentLine = new ArrayList<>();

    final List<List<TextPosition>> currentHeader = new ArrayList<>();
    final List<List<List<TextPosition>>> currentBody = new ArrayList<>();
}
