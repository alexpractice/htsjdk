package htsjdk.samtools.sra;

import htsjdk.samtools.reference.ReferenceSequence;
import htsjdk.samtools.reference.ReferenceSequenceFile;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SRAReferenceTest {
    @DataProvider(name = "testReference")
    public Object[][] createDataForReference() {
        return new Object[][] {
                {"SRR2096940", "CM000681.1", 95001, 95050, "AGATGATTCAGTCTCACCAAGAACACTGAAAGTCACATGGCTACCAGCAT"},
        };
    }

    @Test(dataProvider = "testReference")
    public void testReference(String acc, String refContig, int refStart, int refStop, String refBases) {
        if (!SRAAccession.isSupported()) return;

        ReferenceSequenceFile refSeqFile = new SRAIndexedSequenceFile(new SRAAccession(acc));
        ReferenceSequence refSeq = refSeqFile.getSubsequenceAt(refContig, refStart, refStop);
        Assert.assertEquals(new String(refSeq.getBases()), refBases);
    }
}
