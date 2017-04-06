package cn.fish.gesturechecker.utils;

import java.util.Collections;
import java.util.List;

/**
 * Created by fish on 17-4-5.
 */

public class CollectionUtils {
    private static int containsOrderedCollection(List<?> source, List<?> target) {
        if (source.size() <= 0 || target.size() <= 0) {
            return -1;
        }
        int delta = source.size() - target.size();
        for (int index = 0; index <= delta; index++) {
            if (source.get(index).equals(target.get(index))) {
                for (int i = 0; i < target.size(); i++) {
                    if (!source.get(index + i).equals(target.get(i))) {
                        break;
                    }
                    if (i == target.size() - 1) {
                        return index;
                    }
                }
            }
        }
        return -1;
    }

    public static void replaceCollectionToObj(List source, List target, Object obj) {
        int offset = Collections.indexOfSubList(source, target);
        if (offset == -1) {
            return ;
        }
        int cnt = 0;
        while (cnt <= target.size() -1) {
            source.remove(offset);
            cnt++;
        }
        source.add(offset, obj);
    }
}
