package com.atguigu.b_stringbuilder;

public class Demo03 {
    //# 自主练习题需求
    //**题目**：自定义一个数组，遍历数组元素，**使用StringBuilder完成拼接**，按指定格式拼接后输出结果。
    //**要求**
    //1. 自己定义任意类型数组（整型/字符串数组都行）
    //2. 遍历数组所有元素
    //3. 拼接过程**必须使用StringBuilder**，不能直接用`+`字符串拼接
    //4. 最终拼接成规整格式并控制台打印输出

    public static void main(String[] args) {
        String[] strings={"ab","cd","ef"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
           if (i==0){
               sb.append("[").append(strings[i]).append(",");
           }else if (i==strings.length-1){
               sb.append(strings[i]).append("]");
           }else {
               sb.append(strings[i]).append(",");
           }
        }
        System.out.println(sb);
    }
}
