package com.shixun_mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

class map2 extends Mapper<LongWritable, Text, Text, Text> {
    @Override
    protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, Text>.Context context) throws IOException, InterruptedException {
        String[] strings = value.toString().trim().split(",");
        if (strings[2].length() > 0 && strings[8].length() > 0)
            context.write(new Text(strings[2]), new Text(strings[8]));
    }
}

class reduce2 extends Reducer<Text, Text, Text, Text> {
    HashMap<String, Long> yeezyMap = new HashMap<String, Long>(); // yeezy
    HashMap<String, Long> off_whiteMap = new HashMap<String, Long>(); // off_white
    int n = 0;
    int yeezy = 0, off_white = 0;

    @Override
    protected void reduce(Text key, Iterable<Text> values, Reducer<Text, Text, Text, Text>.Context context) throws IOException, InterruptedException {
        if (key.toString().contains("Yeezy")) {
            for (Text value : values) {
                if (yeezyMap.get(value.toString()) == null) {
                    yeezyMap.put(value.toString(), new Long("1"));
                } else {
                    Long amount = yeezyMap.get(value.toString());
                    amount++;
                    yeezyMap.remove(value.toString());
                    yeezyMap.put(value.toString(), amount);
                }
                yeezy++;
                n++;
            }
        } else {
            for (Text value : values) {
                if (off_whiteMap.get(value.toString()) == null) {
                    off_whiteMap.put(value.toString(), new Long("1"));
                } else {
                    Long amount = off_whiteMap.get(value.toString());
                    amount++;
                    off_whiteMap.remove(value.toString());
                    off_whiteMap.put(value.toString(), amount);
                }
                off_white++;
                n++;
            }
        }
    }

    @Override
    protected void cleanup(Reducer<Text, Text, Text, Text>.Context context) throws IOException, InterruptedException {
        context.write(new Text("Yeezy??????????????????" + yeezy + "\t\n" + "Yeezy???????????????????????????"), new Text(""));
        context.write(new Text("??????" + "\t" + "??????" + "\t" + "?????????"), new Text(""));
        Set<String> yeezyKeySet = yeezyMap.keySet();
        for (String s : yeezyKeySet) {
            context.write(new Text("Yeezy" + ",\"" + s + "\"," + yeezyMap.get(s)), new Text(""));
        }
        context.write(new Text("Off-White??????????????????" + off_white + "\t\n" + "Off-White???????????????????????????"), new Text(""));
        context.write(new Text("??????" + "\t" + "??????" + "\t" + "?????????"), new Text(""));
        Set<String> off_whiteKeySet = off_whiteMap.keySet();
        for (String s : off_whiteKeySet) {
            context.write(new Text("Off-White" + ",\"" + s + "\"," + off_whiteMap.get(s)), new Text(""));
        }
    }
}

public class shixun2 {
    //    2.?????????????????????????????????????????????
    public static void main(String[] args) throws Exception {
        System.setProperty("HADOOP_USER_NAME", "hadoop");  // ??????Hadoop????????????

        // ??????job??????
        Configuration conf = new Configuration();
        conf.set("mapreduce.app-submission.cross-platform", "true");
        Job job = Job.getInstance(conf);

        String input = "/shixun/datas/newSneaker_data.csv";
        String output = "/shixun/output2";

        //??????job??????
        job.setJobName("Job3_shixun2");

        //set??????Jar?????????
        job.setJar("E:\\??????\\Java\\hadoopexam\\target\\hadoopexam-1.0-SNAPSHOT.jar");

        //???????????????
        job.setJarByClass(shixun2.class);

        //map????????????
        job.setMapperClass(map2.class);
//        job.setCombinerClass(reduce.class);
        job.setReducerClass(reduce2.class);

        //map???????????????
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        //reduce???????????????
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        //?????????????????????output?????????
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://master:9000"), conf, "hadoop");
        Path outputPath = new Path(output);
        if (fileSystem.exists(outputPath)) {
            fileSystem.delete(outputPath, true);
        }

        //????????????job?????????????????????????????????????????????????????????????????????
        long startTime = System.currentTimeMillis(); //??????????????????

        FileInputFormat.addInputPath(job, new Path(input));
        FileOutputFormat.setOutputPath(job, outputPath);

        //??????job
        boolean result = job.waitForCompletion(true);
        long endTime = System.currentTimeMillis(); //??????????????????
        System.out.println("????????????????????? " + (endTime - startTime) + "ms");
        System.exit(result ? 0 : -1);
    }
}
