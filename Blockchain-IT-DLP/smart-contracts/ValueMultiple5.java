
public class ValueMultiple5
{
    public static void main(String[] args)
    {
        String originId = args[0];
        String destId = args[1];
        int value = Integer.parseInt(args[2]);
        int nonce = Integer.parseInt(args[3]);
        String signature = args[4];

        System.out.println(value % 5 == 0);
    }
}
