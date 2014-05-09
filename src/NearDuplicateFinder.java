import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.util.*;

/**
 * Created by huascarf on 3/31/14.
 */
public class NearDuplicateFinder {

    int shinglesNumber = 10;
    int shingleLen = 8;
    double threshold = 0.9;
    int cacheSize = 500000;

    LinkedListMultimap<Integer, NDDSig> llMultimap = LinkedListMultimap.create();
    Multimap<Integer,NDDSig> seenNDDSigs =  Multimaps.synchronizedListMultimap(llMultimap);
    List<NDDSig> NDDSigsLL =  Collections.synchronizedList(new LinkedList<NDDSig>());

    public static void main(String[] args) {
        String[] strings = new String[]{
                "Vet, 77, Busted For Obama Death Threat | The Smoking Gun http://t.co/MrTUwxv via @",
                "Vet, 77, Busted For Obama Death Threat http://tinyurl.com/25zyxgp #tcot #tlot #sgp",
                "Playing a show in Chicago, IL at 9:00 PM today at LE PASSAGE http://artistdata.com/a/32on",
                "Playing a show in Cape Girardeau, MO at 9:00 PM today at The Venue http://artistdata.com/a/32ow"};

        NearDuplicateFinder ndf = new NearDuplicateFinder(10,4,0.45);
        for (String doc : strings) {
            Object nearDup = ndf.findNearDuplicate(doc, doc);
            if(nearDup != null)
                System.out.println("Similar:\n" + doc + "\n" + nearDup + "\n\n\n");
        }
    }

    public Object findNearDuplicate(String doc, Object payload) {
        NDDSig currSig = new NDDSig(doc, shingleLen, shinglesNumber,payload);
        return findNearDuplicate(currSig);
    }

    public Object findNearDuplicate(NDDSig currSig) {

        List<Integer> sigHashes = currSig.getSigHashes();
        HashSet<NDDSig> objsWithOneHashInCommon = new HashSet<>();
        for (Integer hash : sigHashes) {
            Collection<NDDSig> vs = seenNDDSigs.get(hash);
            objsWithOneHashInCommon.addAll(vs);
        }

        for (NDDSig nddSig : objsWithOneHashInCommon) {
            double similarity = nddSig.computeSimilarity(currSig);
//            System.out.println(similarity + " " + nddSig + " to " + currSig );
            if(similarity > threshold){
                return nddSig.getPayload();
            }
        }

        //not found any near duplicate, add current NDDSigto seen documents
        for (Integer hash : sigHashes)
            seenNDDSigs.put(hash, currSig);
        NDDSigsLL.add(currSig);

        //remove from cache
        for(int i = 0 ; i <  NDDSigsLL.size() - cacheSize ; i++) {
            NDDSig evicted = NDDSigsLL.remove(NDDSigsLL.size()-1);
            List<Integer> evictedSigHashes = evicted.getSigHashes();
            for (Integer integer : evictedSigHashes) {
                boolean success = llMultimap.remove(integer,evicted);
                System.out.print("");
            }
        }

        return null;
    }

    public NearDuplicateFinder (){}

    public NearDuplicateFinder (int shinglesNumber, int shingleLen, double threshold){
        this.shinglesNumber = shinglesNumber;
        this.shingleLen = shingleLen;
        this.threshold = threshold;
    }



}
