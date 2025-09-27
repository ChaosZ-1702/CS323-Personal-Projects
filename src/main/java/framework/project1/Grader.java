package framework.project1;

import generated.Splc.Splc;
import org.antlr.v4.runtime.*;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Grader {
    private final InputStream sourceStream;
    private final OutputStream outputStream;
    private final OutputStreamWriter writer;

    public Grader(InputStream sourceStream, OutputStream outputStream) {
        this.sourceStream = sourceStream;
        this.outputStream = outputStream;
        this.writer = new OutputStreamWriter(outputStream);
    }

    public Grader(InputStream sourceStream) {
        this(sourceStream, System.out);
    }

    /**
     * This constructor is for testing purposes only during your development.
     * @param testString source code as a string
     */
    public Grader(String testString) {
        this(new ByteArrayInputStream(testString.getBytes(StandardCharsets.UTF_8)));
    }

    public void run() {
        try{
            CharStream input = CharStreams.fromStream(sourceStream);
            Splc lexer = new Splc(input);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            tokens.fill();
            Vocabulary vocabulary = lexer.getVocabulary();
            for (Token token : tokens.getTokens()) {
                this.writer.append(String.format("Token: %s, Raw: %s\n", vocabulary.getSymbolicName(token.getType()), token.getText()));
            }
            this.writer.flush();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
