package Peer;

import Interfaces.IndexServerInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Test 1000 peers sending request each
 * Created by xuzhuchen on 9/29/17.
 */
public class Test {

    double totalTime = 0;
    int n = 1;
    int k = 1;

    Test(int n,int k){
        this.n=n;
        this.k = k;
        TestThread tt = new TestThread();
        for (int i = 0; i < n; i++) {
            Thread t = new Thread(tt);
            t.run();
            try {
                t.join(10*k);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
        new Thread(new mainThread()).run();
    }

    class TestThread implements Runnable {
        @Override
        public void run() {
            try {
                long startTime = System.currentTimeMillis();
                Registry r = LocateRegistry.getRegistry("127.0.0.1", 10000);
                IndexServerInterface rmiService = (IndexServerInterface) r.lookup("register");
                for (int i = 0; i < k; i++) {
                    rmiService.listAll();
                }
                long endTime = System.currentTimeMillis();
                totalTime += endTime - startTime;
            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (NotBoundException e) {
                e.printStackTrace();
            }
        }
    }

    class mainThread implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(100*n);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(n+" peers, each request "+k+" times, totally cost:"+totalTime/n/k+"ms");
        }
    }

    public static void main(String args[]) {
        if (args.length != 2)
            new Test(10,100);
        else
            new Test(Integer.valueOf(args[0]), Integer.valueOf(args[1]));

    }


}
