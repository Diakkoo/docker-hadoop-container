package top.actioncode.filter;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class ActionCodeFilterMapper extends Mapper<LongWritable, Text, Text, Text> {

    private static final int NUM_FIELDS = 7;
    private static final int ACTION_CODE_INDEX = 3;
    private static final int ACTION_CODE_LENGTH = 4;
    private static final String TARGET_SEQUENCE = "01";
    private static final Set<Character> VALID_CHAR_CODES = new HashSet<>(
            Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'l', 'm', 'n', 'o', 'p', 'q')
    );

    @Override
    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
        String line = value.toString().trim();
        if (line.isEmpty()) {
            context.getCounter("DATA_QUALITY", "EMPTY_LINES").increment(1);
            return;
        }

        String[] fields = line.split(",");
        if (fields.length != NUM_FIELDS) {
            context.getCounter("DATA_QUALITY", "INVALID_FIELD_COUNT").increment(1);
            return;
        }

        String fullCode = fields[ACTION_CODE_INDEX].trim();
        if (fullCode.length() != ACTION_CODE_LENGTH) {
            context.getCounter("DATA_QUALITY", "INVALID_CODE_LENGTH").increment(1);
            return;
        }

        boolean hasInValidChar = false;
        for (char c : fullCode.toCharArray()) {
            if (!VALID_CHAR_CODES.contains(c)) {
                context.getCounter("DATA_QUALITY", "INVALID_CHAR_CODE").increment(1);
                hasInValidChar = true;
                break;
            }
        }

        if (hasInValidChar) {
            return;
        }

        context.getCounter("DATA_QUALITY", "VALID_RECORDS").increment(1);

        if (fullCode.contains(TARGET_SEQUENCE)) {
            context.write(new Text(), value);
            context.getCounter("STAT", "SEQUENCE_01_FOUND").increment(1);
        }
    }
    protected static String getTARGET_SEQUENCE() {
        return TARGET_SEQUENCE;
    }
}
