package htsjdk.samtools.cram;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vadim on 25/08/2015.
 */
public class CRAIIndexTest {

    @Test
    public void testFind() throws IOException, CloneNotSupportedException {
        final List<CRAIEntry> index = new ArrayList<CRAIEntry>();

        final int sequenceId = 1;
        CRAIEntry e = new CRAIEntry();
        e.sequenceId = sequenceId;
        e.alignmentStart = 1;
        e.alignmentSpan = 1;
        e.containerStartOffset = 1;
        e.sliceOffset = 1;
        e.sliceSize = 0;
        index.add(e);

        e = e.clone();
        e.sequenceId = sequenceId;
        e.alignmentStart = 2;
        e.alignmentSpan = 1;
        e.containerStartOffset = 2;
        e.sliceOffset = 1;
        e.sliceSize = 0;
        index.add(e);

        e = e.clone();
        e.sequenceId = sequenceId;
        e.alignmentStart = 3;
        e.alignmentSpan = 1;
        e.containerStartOffset = 3;
        e.sliceOffset = 1;
        e.sliceSize = 0;
        index.add(e);

        Assert.assertFalse(allFoundEntriesIntersectQueryInFind(index, sequenceId, 1, 0));

        Assert.assertTrue(allFoundEntriesIntersectQueryInFind(index, sequenceId, 1, 1));
        Assert.assertTrue(allFoundEntriesIntersectQueryInFind(index, sequenceId, 1, 2));
        Assert.assertTrue(allFoundEntriesIntersectQueryInFind(index, sequenceId, 2, 1));
        Assert.assertTrue(allFoundEntriesIntersectQueryInFind(index, sequenceId, 1, 3));

        final int nonExistentSequenceId = 2;
        Assert.assertFalse(allFoundEntriesIntersectQueryInFind(index, nonExistentSequenceId, 2, 1));
        // a query starting beyond all entries:
        Assert.assertFalse(allFoundEntriesIntersectQueryInFind(index, sequenceId, 4, 1));
    }

    private boolean allFoundEntriesIntersectQueryInFind(final List<CRAIEntry> index, final int sequenceId, final int start, final int span) {
        int foundCount = 0;
        for (final CRAIEntry found : CRAIIndex.find(index, sequenceId, start, span)) {
            foundCount++;
            Assert.assertEquals(found.sequenceId, sequenceId);
            boolean intersects = false;
            for (int pos = Math.min(found.alignmentStart, start); pos <= Math.max(found.alignmentStart + found.alignmentSpan, start + span); pos++) {
                if (pos >= found.alignmentStart && pos >= start &&
                        pos <= found.alignmentStart + found.alignmentSpan && pos <= start + span) {
                    intersects = true;
                    break;
                }
            }
            if (!intersects) {
                return false;
            }
        }
        return foundCount > 0;
    }

    @Test
    public void testGetLeftmost() {
        final List<CRAIEntry> index = new ArrayList<CRAIEntry>();
        Assert.assertNull(CRAIIndex.getLeftmost(index));

        final CRAIEntry e1 = new CRAIEntry();
        e1.sequenceId = 1;
        e1.alignmentStart = 2;
        e1.alignmentSpan = 3;
        e1.containerStartOffset = 4;
        e1.sliceOffset = 5;
        e1.sliceSize = 6;
        index.add(e1);
        // trivial case of single entry in index:
        Assert.assertEquals(e1, CRAIIndex.getLeftmost(index));

        final CRAIEntry e2 = new CRAIEntry();
        e2.sequenceId = 1;
        e2.alignmentStart = e1.alignmentStart + 1;
        e2.alignmentSpan = 3;
        e2.containerStartOffset = 4;
        e2.sliceOffset = 5;
        e2.sliceSize = 6;
        index.add(e2);
        Assert.assertEquals(e1, CRAIIndex.getLeftmost(index));
    }

    @Test
    public void testFindLastAlignedEntry() {
        final List<CRAIEntry> index = new ArrayList<CRAIEntry>();
        Assert.assertEquals(-1, CRAIIndex.findLastAlignedEntry(index));

        // Scan all allowed combinations of 10 mapped/unmapped entries and assert the found last aligned entry:
        final int indexSize = 10;
        for (int lastAligned = 0; lastAligned < indexSize; lastAligned++) {
            index.clear();
            for (int i = 0; i < indexSize; i++) {
                final CRAIEntry e = new CRAIEntry();

                e.sequenceId = (i <= lastAligned ? 0 : -1);
                e.alignmentStart = i;
                index.add(e);
            }
            // check expectations are correct before calling findLastAlignedEntry method:
            Assert.assertTrue(index.get(lastAligned).sequenceId != -1);
            if (lastAligned < index.size() - 1) {
                Assert.assertTrue(index.get(lastAligned + 1).sequenceId == -1);
            }
            // assert the the found value matches the expectation:
            Assert.assertEquals(CRAIIndex.findLastAlignedEntry(index), lastAligned);
        }
    }

}
