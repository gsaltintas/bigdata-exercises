import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MapReduceQuery {

    private static class QueryMapper extends Mapper<Object, Text, Text, IntWritable> {

        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            // write your map function
            // identity function:
            // context.write(key, value);
            // counting function:
            StringTokenizer it = new StringTokenizer(value.toString());
            while (it.hasMoreTokens()) {
                word.set(it.nextToken());
                if (word.getLength() > 0 && word.charAt(0) == 'H') {
                    // key value must be serializable and implement the Writable interface
                    context.write(word, one);
                }
            }
        }
    }

    private static class QueryReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values, Context context)
                throws IOException, InterruptedException {
            // we only count distinct words
            result.set(1);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
        if (otherArgs.length != 2) {
            System.err.println("Usage: 2 required params <hdfs_file_in> <hdfs_file_out>");
            System.exit(2);
        }
        // Setup a MapReduce job
        Job job = new Job(conf, "word count");
        job.setJarByClass(MapReduceQuery.class);
              job.setMapperClass(QueryMapper.class);

              // Can you reducer be used as a combiner? If so, enable it.
        // You can also experiment with and without combiner and compare the running
        // time.
        // job.setCombinerClass(QueryReducer.class);
        job.setReducerClass(QueryReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        // at least one reducer, experiment with this parameter
        // job.setNumReduceTasks(8);
        FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
        FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

}
