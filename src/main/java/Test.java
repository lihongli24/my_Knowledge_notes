public class Test {

    public static void main(String[] args){
        String a = "aa";
//        for (int i = 0; i < 10; i++){
//            a = a + i;
//        }

        a = a + new String("bb");
        a = a + new String("cc");
        System.out.println(a);
    }
}
