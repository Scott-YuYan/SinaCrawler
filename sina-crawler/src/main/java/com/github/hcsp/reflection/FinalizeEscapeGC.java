package com.github.hcsp.reflection;

public class FinalizeEscapeGC {
    private static FinalizeEscapeGC SAVE_HOOK = null;

    private void isAlive() {
        System.out.println("yes,i am alive");
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        System.out.println("finalize method executed");
        FinalizeEscapeGC.SAVE_HOOK = this;
    }

    public static void main(String[] args) throws InterruptedException {
        SAVE_HOOK = new FinalizeEscapeGC();

        SAVE_HOOK = null;
        System.gc();
        Thread.sleep(500);
        if (SAVE_HOOK != null) {
            SAVE_HOOK.isAlive();
        } else {
            System.out.println("i am dead");
        }

        SAVE_HOOK = null;
        System.gc();
        Thread.sleep(500);
        if (SAVE_HOOK != null) {
            SAVE_HOOK.isAlive();
        } else {
            System.out.println("i am dead");
        }
        test();
    }

    public static void test() {
        boolean m = false;
        if (m = false) {
            System.out.println("false");
        } else {
            System.out.println("true");
        }
    }
}
