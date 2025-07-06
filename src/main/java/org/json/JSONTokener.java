package org.json;

import java.io.*;

/*
Public Domain.
 */

/**
 * A JSONTokener takes a source string and extracts characters and tokens from
 * it. It is used by the JSONObject and JSONArray constructors to parse
 * JSON source strings.
 *
 * @author JSON.org
 * @version 2014-05-03
 */
public class JSONTokener {
    /**
     * current read character position on the current line.
     */
    private long character;
    /**
     * flag to indicate if the end of the input has been found.
     */
    private boolean eof;
    /**
     * current read index of the input.
     */
    private long index;
    /**
     * current line of the input.
     */
    private long line;
    /**
     * previous character read from the input.
     */
    private char previous;
    /**
     * Reader for the input.
     */
    private final Reader reader;
    /**
     * flag to indicate that a previous character was requested.
     */
    private boolean usePrevious;
    /**
     * the number of characters read in the previous line.
     */
    private long characterPreviousLine;

    public JSONTokener(Reader reader) {
        this.reader = reader.markSupported()
                ? reader
                : new BufferedReader(reader);
        eof = false;
        usePrevious = false;
        previous = 0;
        index = 0;
        character = 1;
        characterPreviousLine = 0;
        line = 1;
    }

    /**
     * Construct a JSONTokener from a string.
     *
     * @param source A source string.
     */
    public JSONTokener(String source) {
        this(new StringReader(source));
    }

    public void back() throws JSONException {
        decrementIndexes();
        usePrevious = true;
        eof = false;
    }

    /**
     * Decrements the indexes for the {@link #back()} method based on the previous character read.
     */
    private void decrementIndexes() {
        index--;
        if (previous == '\r' || previous == '\n') {
            line--;
            character = characterPreviousLine;
        } else if (character > 0) {
            character--;
        }
    }

    /**
     * Checks if the end of the input has been reached.
     *
     * @return true if at the end of the file and we didn't step back
     */
    public boolean end() {
        return eof && !usePrevious;
    }

    /**
     * Get the next character in the source string.
     *
     * @return The next character, or 0 if past the end of the source string.
     * @throws JSONException Thrown if there is an error reading the source string.
     */
    public char next() throws JSONException {
        int c;
        if (usePrevious) {
            usePrevious = false;
            c = previous;
        } else {
            try {
                c = reader.read();
            } catch (IOException exception) {
                throw new JSONException(exception);
            }
        }
        if (c <= 0) { // End of stream
            eof = true;
            return 0;
        }
        incrementIndexes(c);
        previous = (char) c;
        return previous;
    }

    /**
     * Get the last character read from the input or '\0' if nothing has been read yet.
     *
     * @return the last character read from the input.
     */
    protected char getPrevious() {
        return previous;
    }

    /**
     * Increments the internal indexes according to the previous character
     * read and the character passed as the current character.
     *
     * @param c the current character read.
     */
    private void incrementIndexes(int c) {
        if (c > 0) {
            index++;
            if (c == '\r') {
                line++;
                characterPreviousLine = character;
                character = 0;
            } else if (c == '\n') {
                if (previous != '\r') {
                    line++;
                    characterPreviousLine = character;
                }
                character = 0;
            } else {
                character++;
            }
        }
    }

    /**
     * Get the next n characters.
     *
     * @param n The number of characters to take.
     * @return A string of n characters.
     * @throws JSONException Substring bounds error if there are not
     *                       n characters remaining in the source string.
     */
    public String next(int n) {
        if (n == 0) {
            return "";
        }

        char[] chars = new char[n];
        int pos = 0;

        while (pos < n) {
            chars[pos] = next();
            pos += 1;
        }
        return new String(chars);
    }


    /**
     * Get the next char in the string, skipping whitespace.
     *
     * @return A character, or 0 if there are no more characters.
     * @throws JSONException Thrown if there is an error reading the source string.
     */
    public char nextClean() throws JSONException {
        for (; ; ) {
            char c = next();
            if (c == 0 || c > ' ') {
                return c;
            }
        }
    }


    /**
     * Return the characters up to the next close quote character.
     * Backslash processing is done. The formal JSON format does not
     * allow strings in single quotes, but an implementation is allowed to
     * accept them.
     *
     * @param quote The quoting character, either
     *              <code>"</code>&nbsp;<small>(double quote)</small> or
     *              <code>'</code>&nbsp;<small>(single quote)</small>.
     * @return A String.
     * @throws JSONException Unterminated string.
     */
    public String nextString(char quote) throws JSONException {
        char c;
        StringBuilder sb = new StringBuilder();
        for (; ; ) {
            c = next();
            switch (c) {
                case 0:
                case '\n':
                case '\r':
                    throw syntaxError("Unterminated string. " +
                            "Character with int code " + (int) c + " is not allowed within a quoted string.");
                case '\\':
                    c = next();
                    switch (c) {
                        case 'b':
                            sb.append('\b');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'f':
                            sb.append('\f');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 'u':
                            String next = next(4);
                            try {
                                sb.append((char) Integer.parseInt(next, 16));
                            } catch (NumberFormatException e) {
                                throw syntaxError("Illegal escape. " +
                                        "\\u must be followed by a 4 digit hexadecimal number. \\" + next + " is not valid.", e);
                            }
                            break;
                        case '"':
                        case '\'':
                        case '\\':
                        case '/':
                            sb.append(c);
                            break;
                        default:
                            throw syntaxError("Illegal escape. Escape sequence  \\" + c + " is not valid.");
                    }
                    break;
                default:
                    if (c == quote) {
                        return sb.toString();
                    }
                    sb.append(c);
            }
        }
    }

    /**
     * Get the next value. The value can be a Boolean, Double, Integer,
     * JSONArray, JSONObject, Long, or String, or the JSONObject.NULL object.
     *
     * @return An object.
     * @throws JSONException If syntax error.
     */
    public Object nextValue() throws JSONException {
        char c = nextClean();
        return switch (c) {
            case '{' -> {
                back();
                yield new JSONObject(this);
            }
            case '[' -> {
                back();
                yield new JSONArray(this);
            }
            default -> nextSimpleValue(c);
        };
    }

    Object nextSimpleValue(char c) {
        String string;
        switch (c) {
            case '"':
            case '\'':
                return nextString(c);
        }

        /*
         * Handle unquoted text. This could be the values true, false, or
         * null, or it can be a number. An implementation (such as this one)
         * is allowed to also accept non-standard forms.
         *
         * Accumulate characters until we reach the end of the text or a
         * formatting character.
         */

        StringBuilder sb = new StringBuilder();
        while (c >= ' ' && ",:]}/\\\"[{;=#".indexOf(c) < 0) {
            sb.append(c);
            c = next();
        }
        if (!eof) {
            back();
        }

        string = sb.toString().trim();
        if (string.isEmpty()) {
            throw syntaxError("Missing value");
        }
        return JSONObject.stringToValue(string);
    }

    /**
     * Make a JSONException to signal a syntax error.
     *
     * @param message The error message.
     * @return A JSONException object, suitable for throwing
     */
    public JSONException syntaxError(String message) {
        return new JSONException(message + this);
    }

    /**
     * Make a JSONException to signal a syntax error.
     *
     * @param message  The error message.
     * @param causedBy The throwable that caused the error.
     * @return A JSONException object, suitable for throwing
     */
    public JSONException syntaxError(String message, Throwable causedBy) {
        return new JSONException(message + this, causedBy);
    }

    /**
     * Make a printable string of this JSONTokener.
     *
     * @return " at {index} [character {character} line {line}]"
     */
    @Override
    public String toString() {
        return " at " + index + " [character " + character + " line " +
                line + "]";
    }

    /**
     * Closes the underlying reader, releasing any resources associated with it.
     *
     * @throws IOException If an I/O error occurs while closing the reader.
     */
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }
}
