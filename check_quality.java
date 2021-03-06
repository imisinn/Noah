import java.io.File;
import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.util.ArrayList;

public class check_quality{
  public class functions{
    String name;//関数名
    Integer start;//関数の始まりの行数
    Integer end;//関数の終わりの行数
    Integer num_argument;//引数の数
    String file_place;//ファイルのパス
  }

  public class variables{
    String name;//変数名
    Integer num_line;//変数の宣言の行数
    Integer type;//1:変数 2:仮引数
    Integer flag_unused;//使ったかどうかのフラグ　0:使っていない　1:使った
  }

  public class setting_judg{
    Integer nest_max;
    Integer f_line_max;
    Integer f_name_min;
    Integer f_name_max;
    Integer ava_name_min;
    Integer ava_name_max;
    Integer f_argument_max;
    Integer f_cyclomatic;
    Integer max_grobal;
  }

  Integer f_comentout = Integer.valueOf(0);

  void run(String[] args)throws IOException{
    CheckQualityMethod(args[0]);
    output_message(args[0]);
  }

  void output_message(String check_file)throws IOException{
    setting_judg sets = new setting_judg();
    input_setting(sets);
    output_warning(check_file,sets);
  }

  void output_warning(String check_file,setting_judg sets)throws IOException{
    BufferedReader in = new BufferedReader(new FileReader(check_file + ".info.csv"));
    PrintWriter out = new PrintWriter(new FileWriter(check_file + ".warning.csv"));
    String line = new String();

    while((line = in.readLine()) != null){
      String[] infos = line.split(",");
      if(infos[0].equals("MAX_F_NEST")){
        if(Integer.parseInt(infos[infos.length -1]) > sets.nest_max){
          out.println("DEEP_NEST,"+infos[2]+","+infos[3]+","+infos[1]+"のネストが深すぎます。");
        }
      }else if(infos[0].equals("MAX_F_LINE")){
        if(Integer.parseInt(infos[infos.length -1]) > sets.f_line_max){
          out.println("LONG_FUNCTION,"+infos[2]+","+infos[3]+","+infos[1]+"の関数が長すぎます。");
        }
      }else if(infos[0].equals("GOTO")){
        out.println("GOTO,"+infos[1]+","+infos[2]+","+"goto文があります。");
      }else if(infos[0].equals("NAME_FUNCTION") || (infos[0].equals("NAME_VARIABLE") || infos[0].equals("NAME_ARGUMENT"))){
        if(infos[0].equals("NAME_FUNCTION")){
          if(infos[1].length() < sets.f_name_min)out.println("SHORT_FUNCTION_NAME,"+infos[2]+","+infos[3]+","+"関数名"+infos[0]+"が短すぎます。");
          if(infos[1].length() > sets.f_name_max)out.println("LONG_FUNCTION_NAME,"+infos[2]+","+infos[3]+","+"関数名"+infos[0]+"が長すぎます。");
        }else{
          if(infos[1].length() < sets.ava_name_min)out.println("SHORT_VARIABLE_NAME,"+infos[2]+ "," + infos[3] +",変数名"+infos[1]+"短すぎます。");
          if(infos[1].length() > sets.ava_name_max)out.println("LONG_VARIABLE_NAME,"+infos[2]+ "," + infos[3] +",変数名"+infos[1]+ "長すぎます。");
        }
      }else if(infos[0].equals("UNUSED_VARIABLE")){
        out.println("UNUSED_VARIABLE,"+infos[2]+ "," + infos[3]+",変数"+infos[1]+"が未使用の変数です。");
      }else if(infos[0].equals("NUM_F_ARGUMENT")){
        if(Integer.parseInt(infos[infos.length -1]) > sets.f_argument_max){
          out.println("MANY_ARGMENT,"+infos[1]+","+infos[2]+","+infos[3]+",関数"+infos[1]+"の引数が多すぎます。");
        }
      }else if(infos[0].equals("UNUSED_ARGUMENT")){
        out.println("UNUSED_ARGUMENT,"+infos[2]+ "," + infos[3]+ ",仮引数`" + infos[1] + "`は未使用の仮引数です。");
      }else if(infos[0].equals("NUM_CYCLOMATIC")){
        if(Integer.parseInt(infos[infos.length -1]) > sets.f_cyclomatic){
          out.println("MANY_CYCLOMATIC,"+infos[2]+","+infos[3]+","+infos[5]+","+"関数"+infos[1]+"が複雑すぎます(" + infos[5]+")。");
        }
      }else if(infos[0].equals("NUM_GROBAL")){
        if(Integer.parseInt(infos[infos.length -1]) > sets.max_grobal){
          out.println("NAMY_GROBAL_VARIABLE,"+infos[1]+",,"+",グローバル変数が多すぎます(" + infos[2] +")。");
        }
      }
    }

    in.close();
    out.close();
  }

  void input_setting(setting_judg sets)throws IOException{
    BufferedReader in = new BufferedReader(new FileReader("setting.txt"));
    String line = new String();

    while((line = in.readLine()) != null){
      String[] set = line.split(":");
      if(set[0].equals("nest_max"))sets.nest_max = Integer.parseInt(set[1]);
      else if(set[0].equals("f_line_max"))sets.f_line_max = Integer.parseInt(set[1]);
      else if(set[0].equals("f_name_min"))sets.f_name_min = Integer.parseInt(set[1]);
      else if(set[0].equals("f_name_max"))sets.f_name_max = Integer.parseInt(set[1]);
      else if(set[0].equals("ava_name_min"))sets.ava_name_min = Integer.parseInt(set[1]);
      else if(set[0].equals("ava_name_max"))sets.ava_name_max = Integer.parseInt(set[1]);
      else if(set[0].equals("f_argument_max"))sets.f_argument_max = Integer.parseInt(set[1]);
      else if(set[0].equals("f_cyclomatic"))sets.f_cyclomatic = Integer.parseInt(set[1]);
      else if(set[0].equals("max_grobal"))sets.max_grobal = Integer.parseInt(set[1]);
    }

    in.close();
  }

  void CheckQualityMethod(String check_file)throws IOException{
    nest(check_file);
    function_line(check_file);
    check_goto(check_file);
    check_name(check_file);
    pickup_adlint(check_file);
    check_cyclomatic(check_file);
  }

  void check_cyclomatic(String check_file)throws IOException{
    File fileread = new File("adlint/" + check_file + ".c.met.csv");
    File filewrite = new File(check_file + ".info.csv");
    BufferedReader in = new BufferedReader(new FileReader(fileread));
    PrintWriter out = new PrintWriter(new FileWriter(filewrite, true));
    String line;

    while((line = in.readLine()) != null){
      if(line.contains("FN_CYCM")){
        String words[] = line.split(",");
        out.println("NUM_CYCLOMATIC,"+words[2]+","+check_file+","+words[words.length-3]+","+words[words.length-2]+","+words[words.length-1]);
      }
    }

    in.close();
    out.close();
  }

  void pickup_adlint(String check_file)throws IOException{
    File fileread = new File("adlint/" + check_file + ".c.msg.csv");
    File filewrite = new File(check_file + ".info.csv");
    BufferedReader in = new BufferedReader(new FileReader(fileread));
    PrintWriter out = new PrintWriter(new FileWriter(filewrite, true));
    String line;
    File fileread2 = new File("adlint/" + check_file + ".c.met.csv");
    BufferedReader in2 = new BufferedReader(new FileReader(fileread2));

    while((line = in.readLine()) != null){
      String words[] = line.split(",");
      if(words.length >= 5)if(words[5].equals("W0031")){
        String name = words[words.length -1].replace("仮引数 `","").replace("' は、この関数の中で使われていません。","");
        out.println("UNUSED_ARGUMENT,"+name+ ","+words[1]+","+words[2]);
      }
    }

    while((line = in2.readLine()) != null){
      String words[] = line.split(",");
      if(words.length >= 4)if(words[1].equals("FL_STMT"))out.println("SENTE_NUM,"+check_file+","+words[3]);
    }

    in.close();
    out.close();
  }
  void check_name(String check_file)throws IOException{
    ArrayList<variables> List_Variable = new ArrayList<>();
    ArrayList<functions> ListFunc = new ArrayList<>();
    ListFunc = check_function_name(check_file);
    List_Variable = check_variable_name(check_file);
    check_name_unused(check_file,List_Variable,ListFunc);
    check_num_argument(check_file,List_Variable,ListFunc);
  }

  void check_num_argument(String check_file,ArrayList<variables> List_Variable,ArrayList<functions> ListFunc)throws IOException{
    PrintWriter out = new PrintWriter(new FileWriter(check_file + ".info.csv", true));
    BufferedReader in = new BufferedReader(new FileReader("adlint/" + check_file + ".c.met.csv"));
    String line = new String();

    for(functions func: ListFunc){
      func.num_argument = 0;
    }

    while((line = in.readLine()) != null){
      String words[] = line.split(",");
      if(words[1].equals("FN_PARA")){
        for(functions func: ListFunc){
          if(words[2].equals(func.name))func.num_argument = Integer.parseInt(words[words.length -1]);
        }
      }
    }

    for(functions func: ListFunc){
      out.println("NUM_F_ARGUMENT,"+func.name+","+func.file_place+","+func.start+","+func.num_argument);//測定結果の出力
    }

    out.close();
    in.close();
  }

  void check_name_unused(String check_file,ArrayList<variables> List_Variable,ArrayList<functions> ListFunc)throws IOException{
    BufferedReader in = new BufferedReader(new FileReader(check_file));
    PrintWriter out = new PrintWriter(new FileWriter(check_file + ".info.csv", true));
    String line = new String();
    Integer line_count = Integer.valueOf(1);//何行目を検査しているかを記憶する変数

    while((line = in.readLine()) != null){
      line = Comentout(line);
      for(variables avai : List_Variable){
      }
      line_count++;
    }
    for(variables avai: List_Variable)if(avai.flag_unused.equals(1)){
      out.println("UNUSED_VARIABLE," + avai.name + "," + check_file + "," + avai.num_line);//仮引数情報をファイルに出力
    }

    in.close();
    out.close();
  }

  String Comentout(String line){
    line = ComentoutDoubleSlash(line);
    if(line != null)line = RockComent(line);
    //System.out.println(line);
    return line;
  }

  String RockComent(String line){
    String save_line = new String();

    if(f_comentout == 0){//解析部よりも前に/*が無い場合
      if(line.contains("/*") && line.contains("*/")){//行内でロックコメントが完結している場合
        if(!line.substring(0).equals("/")){
          String[] words = line.split("/*");
          save_line += words[0];
        }
        if(!line.substring(line.length() -1).equals("/")){
          String[] words = line.split("*/");
          save_line += words[1];
        }
        if(save_line == null)save_line = "";
        return save_line;
      }else if(line.contains("/*")){//ロックコメントの/*だけがあった場合の処理
        if(!line.substring(0).equals("/")){
          String[] words = line.split("/*");
          save_line += words[0];
        }
        f_comentout = 1;
        if(save_line == null)save_line = "";
        return save_line;
      }
    }else{//解析部よりも前に/*があり、*/がない場合
      if(line.contains("*/")){//ロックコメントの*/だけがあった場合の処理
        if(!line.substring(line.length() -1).equals("/")){
          String[] words = line.split("*/");
          save_line += words[1];
        }
        f_comentout = 0;
        if(save_line == null)save_line = "";
        return save_line;
      }else{
        return "";//ロックコメントの*/が無くコメントアウト内なので空文字を返す
      }
    }
    if(save_line == null)save_line = "";
    return line;
  }

  String ComentoutDoubleSlash(String line){
    String[] words;
    if(line.contains("//")){
      words = line.split("//");
      line = words[0];
    }
    return line;
  }

  ArrayList<variables> check_variable_name(String check_file)throws IOException{
    File fileread = new File("AST.txt");
    File filewrite = new File(check_file + ".info.csv");
    BufferedReader in = new BufferedReader(new FileReader(fileread));
    PrintWriter out = new PrintWriter(new FileWriter(filewrite, true));
    String line,linesave = new String(),arg_saveline = new String();
    Boolean bool = Boolean.valueOf(false);
    Integer count = Integer.valueOf(1);
    ArrayList<variables> List_Variable = new ArrayList<>();
    Integer num_grobal = Integer.valueOf(0);

    while((line = in.readLine()) != null){
      if(line.contains("VarDecl") && !line.contains("ParmVarDecl"))bool = true;//変数の抽出部　始まり
      if(line.contains("line:"))linesave = line;
      if(bool)num_grobal+=check_grobal(line);
      if(bool)List_Variable.add(make_variable(line,linesave,1));
      bool = false;//変数の抽出部　終わり

      //仮引数の抽出部
      if(line.contains("FunctionDecl"))arg_saveline = line;
      if(arg_saveline.contains("used") && (line.contains("ParmVarDecl") && line.contains("used")))List_Variable.add(make_variable(line,arg_saveline,2));

      count++;//行数のカウント
    }

    for(variables avai:List_Variable)if(avai.type == 1)out.println("NAME_VARIABLE," + avai.name + "," + check_file + "," + avai.num_line);//変数名一覧をファイルに出力
    for(variables avai:List_Variable)if(avai.type == 2)out.println("NAME_ARGUMENT," + avai.name + "," + check_file + "," + avai.num_line);//仮引数情報をファイルに出力

    out.println("NUM_GROBAL,"+check_file+","+num_grobal);

    in.close();
    out.close();
    return List_Variable;
  }

  Integer check_grobal(String line)throws IOException{
    Integer num = Integer.valueOf(0);
    String words[] = line.split(" ");
    if(words[0].equals("|-VarDecl"))num = 1;
    return num;
  }

  variables make_variable(String line,String save_line,Integer type)throws IOException{
    String[] words = line.split(" ",-1);
    String name = new String();
    variables avai = new variables();
    Boolean bool = Boolean.valueOf(false);
    String save = new String();
    Integer check_col = Integer.valueOf(0);

    avai.flag_unused = 0;

    avai.type = type;

    for(String word:words){//変数、仮引数の名の取得　
      if(bool == true){
        avai.name = word;
        break;
      }
      if(word.equals("used")){
        bool = true;//used の次に変数名が来るため、目印としてtrueに変更している。
        check_col = 0;
      }

      if(check_col.equals(0) && word.contains("col:"))check_col = 1;

      if(check_col.equals(1) && !word.contains("col:")){
        avai.name = word;
        avai.flag_unused = 1;
        break;
      }

    }

    for(String word:words){
      if(word.contains("line:")){//VarDeclの行数内に行数があるかの検知。
        avai.num_line = pichup_num_line(word);
      }
    }
    if(avai.num_line == null){
      String[] save_words = save_line.split(" ",-1);
      for(String save_word:save_words)if(save_word.contains("line:")){
        Integer line_num = pichup_num_line(save_word);
        avai.num_line = line_num;
      }
    }
    return avai;
  }

  Integer pichup_num_line(String words)throws IOException{
    Integer num = Integer.valueOf(0);
    String[] word = words.split(":",-1);
    if(word.length < 3)return -1;
    if(!isNumber(word[1]))return -2;
    num = Integer.parseInt(word[1]);
    return num;
  }

  Boolean isNumber(String num){
    try{
      Integer.parseInt(num);
      return true;
    } catch (NumberFormatException e){
      return false;
    }
  }


  ArrayList<functions> check_function_name(String check_file)throws IOException{
    File fileread = new File(check_file + ".info.csv");
    File filewrite = new File(check_file + ".info.csv");
    BufferedReader in = new BufferedReader(new FileReader(fileread));
    PrintWriter out = new PrintWriter(new FileWriter(filewrite, true));
    String line;
    ArrayList<functions> ListFunc = new ArrayList<>();
    String file_place = new String();

    while((line = in.readLine()) != null){//関数に関する情報の取得
      String[] metrics_words = line.split(",",-1);
      file_place = metrics_words[2];
      if(metrics_words[0].equals("MAX_F_LINE")){
        ListFunc.add(make_functions(metrics_words));
      }
    }

    for(functions aFunction:ListFunc){
      aFunction.file_place = file_place;
      out.println("NAME_FUNCTION," + aFunction.name + "," + file_place +  "," + aFunction.start + ","+ aFunction.end);//関数名一覧をファイルに出力
    }

    in.close();
    out.close();

    return ListFunc;
  }

  functions make_functions(String[] words)throws IOException{
    functions func = new functions();
    func.name = words[1];
    func.start = Integer.parseInt(words[3]);
    func.end = Integer.parseInt(words[3]) + Integer.parseInt(words[5]) -1;
    return func;
  }

  void check_goto(String check_file)throws IOException{
    File fileread = new File("adlint/" + check_file + ".c.msg.csv");
    File filewrite = new File(check_file + ".info.csv");
    BufferedReader in = new BufferedReader(new FileReader(fileread));
    PrintWriter out = new PrintWriter(new FileWriter(filewrite, true));
    String line;

    while((line = in.readLine()) != null){
      String[] metrics_words = line.split(",",-1);
      if(metrics_words.length >= 5)if(metrics_words[5].equals("W1072"))out.println("GOTO,"+metrics_words[1].replace(".c.c",".c")+","+metrics_words[2]+","+metrics_words[3]);
    }
    in.close();
    out.close();
  }

  void function_line(String check_file)throws IOException{
    File fileread = new File("adlint/" + check_file + ".c.met.csv");
    File filewrite = new File(check_file + ".info.csv");
    BufferedReader in = new BufferedReader(new FileReader(fileread));
    PrintWriter out = new PrintWriter(new FileWriter(filewrite, true));
    String line;

    while((line = in.readLine()) != null){
      String[] metrics_words = line.split(",",-1);
      if(metrics_words[1].equals("FN_LINE"))out.println(make_message(metrics_words,"MAX_F_LINE"));//行数に関する情報の抽出
    }
    in.close();
    out.close();
  }

  void nest(String check_file)throws IOException{
    File fileread = new File("adlint/" + check_file + ".c.met.csv");
    File filewrite = new File(check_file + ".info.csv");
    BufferedReader in = new BufferedReader(new FileReader(fileread));
    PrintWriter out = new PrintWriter(new FileWriter(filewrite));
    String line;

    while((line = in.readLine()) != null){
      String[] metrics_words = line.split(",",-1);
      if(metrics_words[1].equals("FN_NEST"))out.println(make_message(metrics_words,"MAX_F_NEST"));//ネストに関する情報の抽出
    }
    in.close();
    out.close();
  }

  String make_message(String[] metrics_words,String kind){
    Integer len = Integer.valueOf(metrics_words.length);
    String message = new String(kind+","+metrics_words[2]+","+metrics_words[len-4].replace(".c.c",".c")+","+metrics_words[len-3]+","+metrics_words[len-2]+","+metrics_words[len-1]);
    return message;
  }

  public static void main(String[] args)throws IOException{
    check_quality app = new check_quality();
    app.run(args);
  }
}
