/*
 * Copyright 2005-2011 by BerryWorks Software, LLC. All rights reserved.
 *
 * This file is part of EDIReader. You may obtain a license for its use directly from
 * BerryWorks Software, and you may also choose to use this software under the terms of the
 * GPL version 3. Other products in the EDIReader software suite are available only by licensing
 * with BerryWorks. Only those files bearing the GPL statement below are available under the GPL.
 *
 * EDIReader is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * EDIReader is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with EDIReader.  If not,
 * see <http://www.gnu.org/licenses/>.
 */

package com.berryworks.edireader;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.Locale;

import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import com.berryworks.edireader.error.EDISyntaxExceptionHandler;
import com.berryworks.edireader.tokenizer.EDITokenizerNIO;
import com.berryworks.edireader.tokenizer.Tokenizer;
import com.berryworks.edireader.util.BranchingWriter;


/**
 * An adaptor for XMLReader providing default implementations of several methods
 * to simplify each of the EDIReader classes that have XMLReader as an ancestor.
 */
public abstract class EDIAbstractReader implements XMLReader
{

  protected static final String BERRYWORKS_NAMESPACE = "http://www.berryworkssoftware.com/2008/edireader";

  /**
   * The ContentHandler for this XMLReader
   */
  private ContentHandler contentHandler;

  /**
   * The tokenizer used by this EDIAbstractReader
   */
  private Tokenizer tokenizer;

  private EDISyntaxExceptionHandler syntaxExceptionHandler;

  private ErrorHandler errorHandler;

  private EntityResolver entityResolver;

  /**
   * Character marking the boundary between fields
   */
  private char delimiter;

  /**
   * Character marking the boundary between sub-fields
   */
  private char subDelimiter;

  /**
   * Character marking the boundary between sub-sub-fields
   */
  private char subSubDelimiter;

  /**
   * Character used as a decimal point ("." or ",")
   */
  private char decimalMark;

  /**
   * Character marking the boundary between repeating fields
   */
  private char repetitionSeparator;

  /**
   * Character marking the boundary between segments
   */
  private char terminator;

  /**
   * The byte value used as a release or escape character.
   */
  private int release;

  /**
   * Whitespace characters observed to follow the formal segment terminator.
   */
  private String terminatorSuffix;

  /**
   * Where the functional acknowledgements are (optionally) written.
   */
  private BranchingWriter ackStream;

  /**
   * If acknowledgements are being written, should an interchange acknowledgment be included?
   * For ANSI X12, this would be a TA1 segment after the ISA.
   */
  private boolean interchangeAcknowledgment;

  /**
   * Used when producing a copy of the parsed input is needed.
   */
  private Writer copyWriter;

  /**
   * May contain a copy of the initial segment of the interchange.
   */
  private String firstSegment;

  /**
   * XML attributes relating to the EDI interchange
   */
  private final EDIAttributes interchangeAttributes = new EDIAttributes();

  /**
   * Empty attributes object for convenience in starting elements having no attributes
   */
  private final EDIAttributes noAttributes = new EDIAttributes();

  /**
   * XML attributes relating to the EDI structure that ANSI X12 calls a
   * functional group. In EDIFACT, this corresponds to the UNG/UNE structure.
   */
  private final EDIAttributes groupAttributes = new EDIAttributes();

  /**
   * XML attributes relating to the document. In ANSI X12 terminology this
   * would be the Transaction Set (ST/SE). In EDIFACT, it would be a Message
   * (UNH/UNT)..
   */
  private final EDIAttributes documentAttributes = new EDIAttributes();

  private boolean previewed;

  private boolean externalXmlDocumentStart;

  private boolean namespaceEnabled;

  private SyntaxDescriptor acknowledgmentSyntaxDescriptor;

  private TransactionCallback transactionCallback;

  /**
   * Gets the character marking the boundary between segments
   *
   * @return The terminator value
   */
  public char getTerminator()
  {
    return terminator;
  }

  /**
   * Gets the short String of 'whitespace' characters that follows the
   * terminator.
   *
   * @return The terminator value
   */
  public String getTerminatorSuffix()
  {
    return terminatorSuffix;
  }

  public void setDelimiter(char delimiter)
  {
    this.delimiter = delimiter;
  }

  public void setSubDelimiter(char subDelimiter)
  {
    this.subDelimiter = subDelimiter;
  }

  public void setSubSubDelimiter(char subSubDelimiter)
  {
    this.subSubDelimiter = subSubDelimiter;
  }

  public void setDecimalMark(char decimalMark)
  {
    this.decimalMark = decimalMark;
  }

  public void setRepetitionSeparator(char repetitionSeparator)
  {
    this.repetitionSeparator = repetitionSeparator;
  }

  public void setTerminator(char terminator)
  {
    this.terminator = terminator;
  }

  public void setRelease(int release)
  {
    this.release = release;
  }

  public void setTerminatorSuffix(String terminatorSuffix)
  {
    this.terminatorSuffix = terminatorSuffix;
  }

  /**
   * Gets the character marking the boundary between fields
   *
   * @return The delimiter value
   */
  public char getDelimiter()
  {
    return delimiter;
  }

  /**
   * Gets the character marking the boundary between sub-fields. Subfields may
   * be called by different names in different EDI standards.
   *
   * @return The subDelimiter value
   */
  public char getSubDelimiter()
  {
    return subDelimiter;
  }

  /**
   * Gets the character used in release/escape sequences.
   * Exactly how this character is used may differ between standards.
   * In ANSI, there is no release mechanism. When no release character
   * is available, the int value -1 is returned, otherwise a char
   * value is returned via the int.
   *
   * @return The release char value or -1 if none
   */
  public int getRelease()
  {
    return release;
  }

  public char getReleaseCharacter()
  {
    return isReleaseCharacterDefined() ? (char) release : ' ';
  }

  public boolean isReleaseCharacterDefined()
  {
    return release != -1;
  }

  /**
   * Gets the character used as the decimal point in currency.
   * This is the period (".") in the USA and many other countries,
   * but can also be the comma (",").
   *
   * @return mark
   */
  public char getDecimalMark()
  {
    return decimalMark;
  }

  /**
   * Gets the character marking the boundary between sub-sub-fields.
   * Sub-sub-fields are not used in ANSI or EDIFACT, but appear in HL7.
   *
   * @return The subSubDelimiter value
   */
  public char getSubSubDelimiter()
  {
    return subSubDelimiter;
  }

  /**
   * Gets the character marking the boundary between repeating fields.
   *
   * @return The repetitionSeparator value
   */
  public char getRepetitionSeparator()
  {
    return repetitionSeparator;
  }

  public Tokenizer getTokenizer()
  {
    return tokenizer;
  }

  public void setTokenizer(Tokenizer t)
  {
    tokenizer = t;
    if (EDIReader.debug)
      trace("EDIAbstractReader.setTokenizer("
        + ((t == null) ? "null" : "non-null") + ")");
  }

  public void setCopyWriter(Writer writer)
  {
    if (tokenizer != null)
      tokenizer.setWriter(writer);
  }

  protected static Reader createReader(InputSource source) throws IOException
  {
    Reader inputReader;
    if (source == null)
      throw new IOException("createReader called with null InputSource");

    // first try to establish inputReader from the InputSource's
    // CharacterStream
    inputReader = source.getCharacterStream();
    if (inputReader == null)
    {
      InputStream inputStream = source.getByteStream();
      if (inputStream != null)
        // establish inputReader from a ByteStream
        inputReader = new InputStreamReader(inputStream);
      else
      {
        String systemId = source.getSystemId();
        if (systemId != null)
        {
          // try to establish inputReader using the SystemId
          if (systemId.startsWith("file:"))
            // systemId names a file
            inputReader = new FileReader(systemId.substring(5));
          else
            // some kind of URL not yet supported
            throw new IOException("InputSource using SystemId ("
              + systemId + ") not yet supported");
        }
        else
          // getCharacterStream(), getByteStream(), and
          // getSystemId() all return null
          throw new IOException(
            "Cannot get ByteStream, CharacterStream, or SystemId from EDI InputSource");
      }
    }

    return inputReader;
  }

  /**
   * Prepare the parser for its parse method to be called. This involves
   * previewing some of the interchange to discover syntactic details, and
   * making sure a tokenizer is in place. The preview method, of course,
   * varies with each EDI standard.
   *
   * @param source provides read access to the EDI data
   * @throws EDISyntaxException if invalid EDI is detected
   * @throws IOException        if problem reading source
   */
  protected void parseSetup(InputSource source) throws EDISyntaxException,
    IOException
  {
    if (EDIReader.debug)
      trace("EDIAbstractReader.parseSetup()");

    Reader inputReader = createReader(source);

    if (tokenizer == null)
    {
      setTokenizer(new EDITokenizerNIO(inputReader));
      if (EDIReader.debug)
        trace("Constructed new tokenizer because this reader did not have one");
    }
    else if (EDIReader.debug)
      trace("Reusing existing tokenizer");

    if (!previewed)
    {
      if (EDIReader.debug)
        trace("EDIAbstractReader: not yet previewed");
      preview();
      previewed = true;
    }

    if (copyWriter != null)
      tokenizer.setWriter(copyWriter);

    if (EDIReader.debug)
      trace("parseSetup completed");

  }

  /**
   * Preview the EDI interchange to discover syntactic details that will be
   * useful to know before the actual parse method is called.
   *
   * @throws java.io.IOException for problem reading EDI data
   * @throws EDISyntaxException  if invalid EDI is detected
   */
  public abstract void preview() throws EDISyntaxException, IOException;

  /**
   * Indicate that functional acknowledgments are to be generated by
   * designating a Writer. This method should be called before calling parse()
   * if acknowledgments are desired.
   *
   * @param writer The new acknowledgment value
   */
  public void setAcknowledgment(Writer writer)
  {
    ackStream = (writer == null) ? null : new BranchingWriter(writer);
  }

  public void setAcknowledgment(Writer writer, SyntaxDescriptor syntaxDescriptor)
  {
    setAcknowledgment(writer);
    setAcknowledgmentSyntaxDescriptor(syntaxDescriptor);
  }

  public boolean isInterchangeAcknowledgment()
  {
    return interchangeAcknowledgment;
  }

  public void setInterchangeAcknowledgment(boolean interchangeAcknowledgment)
  {
    this.interchangeAcknowledgment = interchangeAcknowledgment;
  }

  public SyntaxDescriptor getAcknowledgmentSyntaxDescriptor()
  {
    return acknowledgmentSyntaxDescriptor;
  }

  public void setAcknowledgmentSyntaxDescriptor(SyntaxDescriptor syntaxDescriptor)
  {
    acknowledgmentSyntaxDescriptor = syntaxDescriptor;
  }

  public TransactionCallback getTransactionCallback()
  {
    return transactionCallback;
  }

  public void setTransactionCallback(TransactionCallback transactionCallback)
  {
    this.transactionCallback = transactionCallback;
  }

  public EDISyntaxExceptionHandler getSyntaxExceptionHandler()
  {
    return syntaxExceptionHandler;
  }

  public void setSyntaxExceptionHandler(EDISyntaxExceptionHandler syntaxExceptionHandler)
  {
    this.syntaxExceptionHandler = syntaxExceptionHandler;
  }

  public boolean isNamespaceEnabled()
  {
    return namespaceEnabled;
  }

  public void setNamespaceEnabled(boolean namespaceEnabled)
  {
    this.namespaceEnabled = namespaceEnabled;
  }

  public boolean isExternalXmlDocumentStart()
  {
    return externalXmlDocumentStart;
  }

  public void setExternalXmlDocumentStart(boolean externalXmlDocumentStart)
  {
    this.externalXmlDocumentStart = externalXmlDocumentStart;
  }

  public void setLocale(Locale locale) throws SAXException
  {
    throw new SAXNotSupportedException("setLocale not supported");
  }

  public void setEntityResolver(EntityResolver resolver)
  {
    entityResolver = resolver;
  }

  public void setDTDHandler(DTDHandler handler)
  {
  }

  public void setErrorHandler(ErrorHandler handler)
  {
    errorHandler = handler;
  }

  /**
   * Parse the EDI interchange. Each subclass must override this method.
   */
  public void parse(String systemId) throws SAXException, IOException
  {
    throw new SAXException("parse(systemId) not supported");
  }

  public void setContentHandler(ContentHandler handler)
  {
    contentHandler = handler;
  }

  public ContentHandler getContentHandler()
  {
    return contentHandler;
  }

  public void setFeature(String name, boolean value)
    throws SAXNotRecognizedException, SAXNotSupportedException
  {
  }

  public boolean getFeature(String name) throws SAXNotRecognizedException,
    SAXNotSupportedException
  {
    throw new SAXNotSupportedException("Not yet implemented");
  }

  public void setProperty(String name, Object value)
    throws SAXNotRecognizedException, SAXNotSupportedException
  {
  }

  public Object getProperty(String name) throws SAXNotRecognizedException,
    SAXNotSupportedException
  {
    throw new SAXNotSupportedException("Not yet implemented");
  }

  public EDIAttributes getDocumentAttributes()
  {
    return documentAttributes;
  }

  public EDIAttributes getInterchangeAttributes()
  {
    return interchangeAttributes;
  }

  public EDIAttributes getGroupAttributes()
  {
    return groupAttributes;
  }

  public BranchingWriter getAckStream()
  {
    return ackStream;
  }

  public void setAckStream(BranchingWriter ackStream)
  {
    this.ackStream = ackStream;
  }

  public boolean isPreviewed()
  {
    return previewed;
  }

  public void setPreviewed(boolean previewed)
  {
    this.previewed = previewed;
  }

  public ErrorHandler getErrorHandler()
  {
    return errorHandler;
  }

  public DTDHandler getDTDHandler()
  {
    return null;
  }

  public EntityResolver getEntityResolver()
  {
    return entityResolver;
  }

  public int getCharCount()
  {
    return tokenizer == null ? 0 : tokenizer.getCharCount();
  }

  public int getSegmentCharCount()
  {
    return tokenizer == null ? 0 : tokenizer.getSegmentCharCount();
  }

  public String getFirstSegment()
  {
    return firstSegment;
  }

  public void setFirstSegment(String firstSegment)
  {
    this.firstSegment = firstSegment;
  }

  /**
   * Write a message to a diagnostic trace stream.
   *
   * @param msg to appear in trace
   */
  public static void trace(String msg)
  {
    System.err.println(msg);
  }

  /**
   * Write a message to a diagnostic trace stream.
   *
   * @param e Exception to appear in trace
   */
  protected static void trace(Exception e)
  {
    System.err.println(e.toString());
  }

  @Override
  public String toString()
  {
    String lineBreak = System.getProperty("line.separator");
    return lineBreak + "EDIReader summary:" + lineBreak +
      " class: " + getClass().getName() + lineBreak +
      " delimiter: " + getDelimiter() + lineBreak +
      " subDelimiter: " + getSubDelimiter() + lineBreak +
      " subSubDelimiter: " + getSubSubDelimiter() + lineBreak +
      " repetitionSeparator: " + getRepetitionSeparator() + lineBreak +
      " terminator: " + getTerminator() + lineBreak +
      " terminatorSuffix: " + getTerminatorSuffix() + lineBreak +
      " charCount: " + getCharCount() + lineBreak +
      " segmentCharCount: " + getSegmentCharCount() + lineBreak;
  }

  /**
   * Determine if a String argument is null and not empty.
   *
   * @param value
   * @return false if the argument is null or empty, and true otherwise
   */
  public static boolean isPresent(String value)
  {
    return value != null && value.length() > 0;
  }

  /**
   * Return the String argument unless that argument is null,
   * in which case an empty String is returned instead.
   *
   * @param value
   * @return value, but "" if the value is null
   */
  public static String emptyStringIfNull(String value)
  {
    return value == null ? "" : value;
  }
}
