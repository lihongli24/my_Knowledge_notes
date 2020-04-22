import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class Test {

//    public static void main(String[] args){
////        String a = "aa";
//////        for (int i = 0; i < 10; i++){
//////            a = a + i;
//////        }
////
////        a = a + new String("bb");
////        a = a + new String("cc");
////        System.out.println(a);
//        int result = factorial(4);
//        System.out.println(result);
//    }

//    private static int factorial(int n){
//        if(n == 1){
//            return n;
//        }else {
//            return n * factorial(n-1);
//        }
//    }

    @Data
    static class Child extends Parent{
        int name;

        public int getName() {
            return name;
        }
    }

    static class Parent{
        int age;

        public int getAge() {
            return age;
        }
    }

    public static void main(String[] args){
//        Child child = new Child();
//        child.name = 8;
//
//        System.out.print(child);
//        Boolean bb = null;
//        if(bb == false){
//            System.out.println("aa");
//        }

//        Object a = null;
//        Child result = (Child)a;
//        System.out.println("a");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("y-MMMMM-dd", Locale.ENGLISH);
        Date date =  new Date();
        System.out.println(simpleDateFormat.format(date));
    }


}
