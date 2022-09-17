package com.hive;


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
import java.util.HashMap;

class map6 extends Mapper<LongWritable, Text, Text, LongWritable> {
    @Override
    protected void map(LongWritable key, Text value, Mapper<LongWritable, Text, Text, LongWritable>.Context context) throws IOException, InterruptedException {
        byte[] bytes = value.getBytes();
        String str = new String(bytes, 0, value.getLength(), "GBK");
        String[] strings = str.trim().split(",");
        if (strings!=null&&strings.length>8&&strings[4].trim().length()>0&&strings[1].trim().length()>0&&strings[8].trim().length()>0) {
            int year = Integer.parseInt(strings[4]);
            int month = Integer.parseInt(strings[1]);
            context.write(new Text(year + "-" + month + " " + strings[8]), new LongWritable(1));
        }
    }
}

class reduce6 extends Reducer<Text, LongWritable, Text, Text> {
    HashMap<String, Long> map = new HashMap<String, Long>();
    int n = 0;
    double num = 0;//总车辆数
    long man = 0, woman = 0;

    @Override
    protected void setup(Reducer<Text, LongWritable, Text, Text>.Context context) throws IOException, InterruptedException {
        context.write(new Text("月份" + "\t\t" +"车辆类型" + "\t\t" + "数量"), new Text(""));
    }

    @Override
    protected void reduce(Text key, Iterable<LongWritable> values, Reducer<Text, LongWritable, Text, Text>.Context context) throws IOException, InterruptedException {
        long sum = 0;

        for (LongWritable value : values) {
            sum += value.get();
        }
        context.write(key, new Text(sum + ""));
    }

}

public class hive3_1 {
    public static void main(String[] args) throws Exception {
        System.setProperty("HADOOP_USER_NAME", "hadoop");  // 使用Hadoop用户权限

        // 创建job实例
        Configuration conf = new Configuration();
        conf.set("mapreduce.app-submission.cross-platform", "true");
        Job job = Job.getInstance(conf);

//        String input = "/hivetest/test/11 汽车销售数据-Cars.csv";
        String input = "/hivetest/cars.csv";
        String output = "/hivetest/output/test3.1";

        //设置job名称
        job.setJobName("hive3.1");

        //set创建Jar包位置
        job.setJar("E:\\程序\\Java\\hadoopexam\\target\\hadoopexam-1.0-SNAPSHOT.jar");

        //主函数位置
        job.setJarByClass(hive3_1.class);

        //map函数位置
        job.setMapperClass(map6.class);
        job.setReducerClass(reduce6.class);

        //map设置出来的
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);

        //reduce设置出来的
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        //删除已经存在的output文件夹
        FileSystem fileSystem = FileSystem.get(new URI("hdfs://master:9000"), conf, "hadoop");
        Path outputPath = new Path(output);
        if (fileSystem.exists(outputPath)) {
            fileSystem.delete(outputPath, true);
        }

        //指定本次job待处理数据的目录和程序执行完输出结果存放的目录
        long startTime = System.currentTimeMillis(); //获取当前时间

        FileInputFormat.addInputPath(job, new Path(input));
        FileOutputFormat.setOutputPath(job, outputPath);

        //提交job
        boolean result = job.waitForCompletion(true);
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("程序运行时间： " + (endTime - startTime) + "ms");
        System.exit(result ? 0 : -1);
    }
}
