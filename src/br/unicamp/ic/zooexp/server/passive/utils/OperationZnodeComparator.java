package br.unicamp.ic.zooexp.server.passive.utils;

import java.util.Comparator;

public class OperationZnodeComparator implements Comparator<String> {

    /**
     * Compare znode names only in respect to the sequencial number added to them.
     * The goal is to order ephemeral znodes from the same parent, establishing a
     * before-happens relation
     *
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(String str1, String str2) {
        int index1 = str1.lastIndexOf('-');
        int index2 = str2.lastIndexOf('-');
        Integer seqNum1 = Integer.parseInt(str1.substring(index1+1));
        Integer seqNum2 = Integer.parseInt(str2.substring(index2+1));
        return seqNum1.compareTo(seqNum2);

    }

}
