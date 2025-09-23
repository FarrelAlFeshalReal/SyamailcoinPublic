import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

class Realgenesiss$VirtualNAND {
   private final ConcurrentHashMap<String, byte[]> storage = new ConcurrentHashMap();
   private final ReentrantLock lock = new ReentrantLock();

   private Realgenesiss$VirtualNAND() {
   }

   void store(String var1, byte[] var2) {
      this.lock.lock();

      try {
         this.storage.put(var1, var2);
      } finally {
         this.lock.unlock();
      }

   }

   byte[] retrieve(String var1) {
      return (byte[])this.storage.getOrDefault(var1, new byte[0]);
   }
} 
