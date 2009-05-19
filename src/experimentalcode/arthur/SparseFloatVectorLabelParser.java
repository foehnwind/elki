package experimentalcode.arthur;

import de.lmu.ifi.dbs.elki.data.SparseFloatVector;
import de.lmu.ifi.dbs.elki.parser.ParsingResult;
import de.lmu.ifi.dbs.elki.parser.RealVectorLabelParser;
import de.lmu.ifi.dbs.elki.utilities.ClassGenericsUtil;
import de.lmu.ifi.dbs.elki.utilities.Util;
import de.lmu.ifi.dbs.elki.utilities.pairs.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Provides a parser for parsing one point per line, attributes separated by
 * whitespace.
 * </p>
 * <p>
 * Several labels may be given per point. A label must not be parseable as
 * double. Lines starting with &quot;#&quot; will be ignored.
 * </p>
 * <p>A line is expected in the following format:
 * The first entry of each line is the number of attributes with coordinate value not zero.
 * Subsequent entries are of the form (index, value),
 * where index is the number of the corresponding dimension,
 * and value is the value of the corresponding attribute.</p>
 * <p>
 * An index can be specified to identify an entry to be treated as class label.
 * This index counts all entries (numeric and labels as well) starting with 0.
 * </p>
 * 
 * @author Arthur Zimek
 */
public class SparseFloatVectorLabelParser extends RealVectorLabelParser<SparseFloatVector> {

  private int dimensionality = -1;
  
  /**
   * Creates a DoubleVector out of the given attribute values.
   * 
   * @see de.lmu.ifi.dbs.elki.parser.RealVectorLabelParser#createDBObject(java.util.List)
   */
  @Override
  public SparseFloatVector createDBObject(List<Double> attributes) {
    return new SparseFloatVector(Util.unboxToFloat(ClassGenericsUtil.toArray(attributes, Double.class)));
  }

  @Override
  protected String descriptionLineType() {
    return "The values will be parsed as floats (resulting in a set of SparseFloatVectors). A line is expected in the following format: The first entry of each line is the number of attributes with coordinate value not zero. Subsequent entries are of the form (index, value), where index is the number of the corresponding dimension, and value is the value of the corresponding attribute.";
  }

  /* (non-Javadoc)
   * @see de.lmu.ifi.dbs.elki.parser.RealVectorLabelParser#parseLine(java.lang.String)
   */
  @Override
  public Pair<SparseFloatVector, List<String>> parseLine(String line) {
    String[] entries = WHITESPACE_PATTERN.split(line);
    int cardinality = Integer.parseInt(entries[0]);
    
    Map<Integer, Float> values = new HashMap<Integer, Float>(cardinality,1);
    List<String> labels = new ArrayList<String>();
    
    for(int i = 1; i < entries.length-1; i++){
      if(i != classLabelIndex) {
        Integer index;
        Float attribute;
        try {
          index = Integer.valueOf(entries[i]);
          if(index > dimensionality) {
            dimensionality = index;
          }
          i++;
        }
        catch(NumberFormatException e) {
          labels.add(entries[i]);
          continue;
        }
        attribute = Float.valueOf(entries[i]);
        values.put(index,attribute);        
      }
      else {
        labels.add(entries[i]);
      }
    }
    return new Pair<SparseFloatVector, List<String>>(new SparseFloatVector(values,dimensionality),labels);
  }
  
  @Override
  public ParsingResult<SparseFloatVector> parse(InputStream in) {
    dimensionality = -1;
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    int lineNumber = 1;
    List<Pair<SparseFloatVector, List<String>>> objectAndLabelsList = new ArrayList<Pair<SparseFloatVector, List<String>>>();
    try {
      for(String line; (line = reader.readLine()) != null; lineNumber++) {
        if(!line.startsWith(COMMENT) && line.length() > 0) {
          objectAndLabelsList.add(parseLine(line));
        }
      }
    }
    catch(IOException e) {
      throw new IllegalArgumentException("Error while parsing line " + lineNumber + ".");
    }
    for(Pair<SparseFloatVector, List<String>> pair : objectAndLabelsList){
      pair.getFirst().setDimensionality(dimensionality);
    }
    return new ParsingResult<SparseFloatVector>(objectAndLabelsList);
  }

  /* (non-Javadoc)
   * @see de.lmu.ifi.dbs.elki.parser.RealVectorLabelParser#parameterDescription()
   */
  @Override
  public String parameterDescription() {
    StringBuffer description = new StringBuffer();
    description.append(this.getClass().getName());
    description.append(" expects following format of parsed lines:\n");
    description.append("A single line provides a single point. Entries are separated by whitespace (");
    description.append(WHITESPACE_PATTERN.pattern()).append("). ");
    description.append(descriptionLineType());
    description.append("Any pair of two subsequent substrings not containing whitespace is tried to be read as int and float. If this fails for the first of the pair (interpreted ans index), it will be appended to a label. (Thus, any label must not be parseable as Integer.) If the float component is not parseable, an exception will be thrown. Empty lines and lines beginning with \"");
    description.append(COMMENT);
    description.append("\" will be ignored. Having the file parsed completely, the maximum occuring dimensionality is set as dimensionality to all created SparseFloatvectors.\n");

    return usage(description.toString());
  }

  
  
}
