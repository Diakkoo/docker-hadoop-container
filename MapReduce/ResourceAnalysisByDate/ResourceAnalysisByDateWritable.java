package top.analysis;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.Writable;

public class ResourceAnalysisByDateWritable implements Writable {
    private long totalUsage;
    private long totalReturn;

    public ResourceAnalysisByDateWritable() {
        this.totalUsage = 0;
        this.totalReturn = 0;
    }

    public ResourceAnalysisByDateWritable(long usage, long returned) {
        this.totalUsage = usage;
        this.totalReturn = returned;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeLong(totalUsage);
        out.writeLong(totalReturn);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        totalUsage = in.readLong();
        totalReturn = in.readLong();
    }

    public void merge(ResourceAnalysisByDateWritable other) {
        this.totalUsage += other.getTotalUsage();
        this.totalReturn += other.getTotalReturn();
    }

    public long getTotalUsage() { return totalUsage; }
    public long getTotalReturn() { return totalReturn; }

    @Override
    public String toString() {
        return totalUsage + "\t" + totalReturn;
    }
}
