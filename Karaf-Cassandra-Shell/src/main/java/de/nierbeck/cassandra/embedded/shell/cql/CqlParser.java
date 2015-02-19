package de.nierbeck.cassandra.embedded.shell.cql;

import java.util.ArrayList;
import java.util.List;

import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.api.console.CommandLine;
import org.apache.karaf.shell.api.console.Parser;
import org.apache.karaf.shell.api.console.Session;
import org.apache.karaf.shell.support.parsing.CommandLineImpl;

@Service
public class CqlParser implements Parser {

	@Override
	  public CommandLine parse(Session session, String command, int cursor) {
		
	      MyParser parser = new MyParser(command, cursor);
	      List<String> args = parser.statement();
	      
	      return new CommandLineImpl(
	              args.toArray(new String[args.size()]),
	              parser.cursorArgumentIndex(),
	              parser.argumentPosition(),
	              cursor,
	              command.substring(0, parser.position()));
	  }

	@Override
	public String preprocess(Session session, CommandLine cmdLine) {
		StringBuilder parsed = new StringBuilder();
		for (int i = 0 ; i < cmdLine.getArguments().length; i++) {
			String arg = cmdLine.getArguments()[i];
			if (i > 0) {
				parsed.append(" ");
			}
			for (int j = 0; j < arg.length(); j++) {
				char ch = arg.charAt(j);
				if (ch == '{' || ch == '}' || ch == '\'' || ch == '(' || ch == ')') {
					parsed.append('\\');
				}
				parsed.append(ch);
			}
		}
		return parsed.toString();
	}
	
	/**
	 * class copied from the GogoParser, it just simplifies the way the parser works. 
	 */
	public class MyParser {

	    int current = 0;
	    String text;
	    boolean escaped;
	    static final String SPECIAL = "<;|{[\"'$`(=";

	    List<List<List<String>>> program;
	    List<List<String>> statements;
	    List<String> statement;
	    int cursor;
	    int start = -1;
	    int c0;
	    int c1;
	    int c2;
	    int c3;
	    
	    public MyParser(String text, int cursor) {
	    	this.text = text;
	        this.cursor = cursor;
		}

		public void ws() {
	        // derek: BUGFIX: loop if comment  at beginning of input
	        //while (!eof() && isWhitespace(peek())) {
	        while (!eof() && (!escaped && isWhitespace(peek()) || current == 0)) {
	            if (current != 0 || !escaped && isWhitespace(peek())) {
	                current++;
	            }
	            if (peek() == '/' && current < text.length() - 2
	                && text.charAt(current + 1) == '/') {
	                comment();
	            }
	            if (current == 0) {
	                break;
	            }
	        }
	    }

	    private boolean isWhitespace(char ch) {
	        return ch != '\n' && Character.isWhitespace(ch);
	    }

	    private void comment() {
	        while (!eof() && peek() != '\n' && peek() != '\r') {
	            next();
	        }
	    }

	    public boolean eof() {
	        return current >= text.length();
	    }

	    public char peek() {
	        return peek(false);
	    }

	    char peek(boolean increment) {
	        escaped = false;
	        if (eof()) {
	            return 0;
	        }

	        int last = current;
	        char c = text.charAt(current++);

	        if (c == '\\') {
	            escaped = true;
	            if (eof()) {
	                throw new RuntimeException("Eof found after \\");
	            }

	            c = text.charAt(current++);

	            switch (c) {
	                case 't':
	                    c = '\t';
	                    break;
	                case '\r':
	                case '\n':
	                    c = ' ';
	                    break;
	                case 'b':
	                    c = '\b';
	                    break;
	                case 'f':
	                    c = '\f';
	                    break;
	                case 'n':
	                    c = '\n';
	                    break;
	                case 'r':
	                    c = '\r';
	                    break;
	                case 'u':
	                    c = unicode();
	                    current += 4;
	                    break;
	                default:
	                    // We just take the next character literally
	                    // but have the escaped flag set, important for {},[] etc
	            }
	        }
	        if (cursor > last && cursor <= current) {
	            c0 = program != null ? program.size() : 0;
	            c1 = statements != null ? statements.size() : 0;
	            c2 = statement != null ? statement.size() : 0;
	            c3 = (start >= 0) ? current - start : 0;
	        }
	        if (!increment) {
	            current = last;
	        }
	        return c;
	    }

	    public List<List<List<String>>> program() {
	        program = new ArrayList<List<List<String>>>();
	        ws();
	        if (!eof()) {
	            program.add(pipeline());
	            while (peek() == ';' || peek() == '\n') {
	                current++;
	                List<List<String>> pipeline = pipeline();
	                program.add(pipeline);
	            }
	        }
	        if (!eof()) {
	            throw new RuntimeException("Program has trailing text: " + context(current));
	        }

	        List<List<List<String>>> p = program;
	        program = null;
	        return p;
	    }

	    CharSequence context(int around) {
	        return text.subSequence(Math.max(0, current - 20), Math.min(text.length(),
	            current + 4));
	    }

	    public List<List<String>> pipeline() {
	        statements = new ArrayList<List<String>>();
	        statements.add(statement());
	        while (peek() == '|') {
	            current++;
	            ws();
	            if (!eof()) {
	                statements.add(statement());
	            }
	            else {
	                statements.add(new ArrayList<String>());
	                break;
	            }
	        }
	        List<List<String>> s = statements;
	        statements = null;
	        return s;
	    }

	    public List<String> statement() {
	        statement = new ArrayList<String>();
	        statement.add(value());
	        while (!eof()) {
	            ws();
	            if (peek() == '|' || peek() == ';' || peek() == '\n') {
	                break;
	            }

	            if (!eof()) {
	                statement.add(messy());
	            }
	        }
	        List<String> s = statement;
	        statement = null;
	        return s;
	    }

	    public String messy()
	    {
	        start = current;
	        char c = peek();
	        if (c > 0 && SPECIAL.indexOf(c) < 0) {
	            current++;
	            try {
	                while (!eof()) {
	                    c = peek();
	                    if (!escaped && (c == ';' || c == '|' || c == '\n' || isWhitespace(c))) {
	                        break;
	                    }
	                    next();
	                }
	                return text.substring(start, current);
	            } finally {
	                start = -1;
	            }
	        }
	        else {
	            return value();
	        }
	    }

	    public int position() {
	        return current;
	    }

	    public int cursorArgumentIndex() {
	        return c2;
	    }

	    public int argumentPosition() {
	        return c3;
	    }

	    public String value() {
	        ws();

	        start = current;
	        try {
	            char c = next();
	            if (!escaped) {
	                switch (c) {
	                    case '<':
	                        return text.substring(start, find('>', '<'));
	                    case '=':
	                        return text.substring(start, current);
	                }
	            }

	            // Some identifier or number
	            while (!eof()) {
	                c = peek();
	                if (!escaped) {
	                    if (isWhitespace(c) || c == ';' || c == '|' || c == '=') {
	                        break;
	                    }
	                    else if (c == '<') {
	                        next();
	                        find('>', '<');
	                    }
	                    else {
	                        next();
	                    }
	                }
	                else {
	                    next();
	                }
	            }
	            return text.substring(start, current);
	        } finally {
	            start = -1;
	        }
	    }

	    public boolean escaped() {
	        return escaped;
	    }

	    public char next() {
	        return peek(true);
	    }

	    char unicode() {
	        if (current + 4 > text.length()) {
	            throw new IllegalArgumentException("Unicode \\u escape at eof at pos ..."
	                + context(current) + "...");
	        }

	        String s = text.subSequence(current, current + 4).toString();
	        int n = Integer.parseInt(s, 16);
	        return (char) n;
	    }

	    int find(char target, char deeper) {
	        int start = current;
	        int level = 1;

	        while (level != 0) {
	            if (eof()) {
	                throw new RuntimeException("Eof found in the middle of a compound for '"
	                    + target + deeper + "', begins at " + context(start));
	            }

	            char c = next();
	            if (!escaped) {
	                if (c == target) {
	                    level--;
	                } else {
	                    if (c == deeper) {
	                        level++;
	                    } else {
	                        if (c == '"') {
	                            quote('"');
	                        } else {
	                            if (c == '\'') {
	                                quote('\'');
	                            }
	                            else {
	                                if (c == '`') {
	                                    quote('`');
	                                }
	                            }
	                        }
	                    }
	                }
	            }
	        }
	        return current;
	    }

	    int quote(char which) {
	        while (!eof() && (peek() != which || escaped)) {
	            next();
	        }

	        return current++;
	    }

	    CharSequence findVar() {
	        int start = current;
	        char c = peek();

	        if (Character.isJavaIdentifierPart(c)) {
	            while (c == '$') {
	                c = next();
	            }
	            while (!eof() && (Character.isJavaIdentifierPart(c) || c == '.') && c != '$') {
	                next();
	                c = peek();
	            }
	            return text.subSequence(start, current);
	        }
	        throw new IllegalArgumentException(
	            "Reference to variable does not match syntax of a variable: "
	                + context(start));
	    }

	    public String toString() {
	        return "..." + context(current) + "...";
	    }

	    public String unescape() {
	        StringBuilder sb = new StringBuilder();
	        while (!eof()) {
	            sb.append(next());
	        }
	        return sb.toString();
	    }

	}

	
}
