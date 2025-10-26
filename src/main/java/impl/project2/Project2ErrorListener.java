package impl.project2;

import framework.project2.Grader;
import framework.project2.MissingSymbolError;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Project2ErrorListener extends BaseErrorListener {
    private final Grader grader;
    public Project2ErrorListener(Grader grader) {
        this.grader = grader;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        Matcher missingMatcher = Pattern.compile("missing '([^']+)'", Pattern.CASE_INSENSITIVE).matcher(msg);
        Matcher singleExpectMatcher = Pattern.compile("extraneous input '([^']+)' expecting '([^']+)'", Pattern.CASE_INSENSITIVE).matcher(msg);
        Matcher expectListMatcher = Pattern.compile("extraneous input '([^']+)' expecting \\{([^}]+)\\}", Pattern.CASE_INSENSITIVE).matcher(msg);

        if (singleExpectMatcher.find()) {
            String expected = singleExpectMatcher.group(2);
            missingMatcher = Pattern.compile("missing '([^']+)'", Pattern.CASE_INSENSITIVE).matcher("missing '" + expected + "'");
        } else if (expectListMatcher.find()) {
            String expectList = expectListMatcher.group(2);
            String expected = expectList.split(",")[0].trim();
            if (expected.startsWith("'") && expected.endsWith("'") && expected.length() >= 2) {
                expected = expected.substring(1, expected.length() - 1);
            }
            missingMatcher = Pattern.compile("missing '([^']+)'", Pattern.CASE_INSENSITIVE).matcher("missing '" + expected + "'");
        }
        if (!missingMatcher.find()) {
            this.grader.getWriter().println(msg);
            throw new ParseCancellationException();
        }

        String missing = "'" + missingMatcher.group(1) + "'";

        // 由msg中的符号得到其名称, ';' -> 'SEMI'
        String symbolicName = null;
        if (recognizer instanceof Parser parser) {
            Vocabulary vocab = parser.getVocabulary();
            ATN atn = parser.getATN();
            int maxType = atn.maxTokenType;
            for (int t = 0; t <= maxType; t++) {
                String lit = vocab.getLiteralName(t);
                if (lit != null && lit.equals(missing)) {
                    symbolicName = vocab.getSymbolicName(t);
                    break;
                }
            }
        }

        // 行号提取
        int resultLine = 0;
        try {
            if (offendingSymbol instanceof Token token && recognizer instanceof Parser) {
                TokenStream tokens = ((Parser) recognizer).getInputStream();
                int idx = token.getTokenIndex();
                Token prev = null;
                for (int i = idx - 1; i >= 0; i--) {
                    Token t = tokens.get(i);
                    if (t.getType() == Token.EOF) continue;
                    prev = t;
                    break;
                }
                if (prev != null) {
                    resultLine = prev.getLine();
                } else {
                    resultLine = token.getLine();
                }
            } else {
                resultLine = line;
            }
        } catch (Exception ex) {
            resultLine = line;
        }
        // 打印时行数从0开始，与getLine值相差1
        MissingSymbolError missingSymbol = new MissingSymbolError(symbolicName, resultLine-1);
        this.grader.getWriter().println(missingSymbol);
        throw new ParseCancellationException();
    }
}
