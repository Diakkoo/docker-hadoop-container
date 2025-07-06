package top.analysis;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Writable;

public class HourlyResourceWritable implements Writable {
    private long usedBytes;
    private long returnedBytes;

    public HourlyResourceWritable() {
        this.usedBytes = 0;
        this.returnedBytes = 0;
    }

    public HourlyResourceWritable(long usedBytes, long returnedBytes) {
        this.usedBytes = usedBytes;
        this.returnedBytes = returnedBytes;
    }

    public void set(long usedBytes, long returnedBytes) {
        this.usedBytes = usedBytes;
        this.returnedBytes = returnedBytes;
    }

    public long getUsedBytes() {
        return usedBytes;
    }

    public long getReturnedBytes() {
        return returnedBytes;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeLong(usedBytes);
        out.writeLong(returnedBytes);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        usedBytes = in.readLong();
        returnedBytes = in.readLong();
    }

    @Override
    public String toString() {
        return usedBytes + "\t" + returnedBytes;
    }
}