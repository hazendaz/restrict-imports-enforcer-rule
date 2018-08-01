package de.skuzzle.enforcer.restrictimports.analyze;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SourceTreeAnalyzerImplTest {

    @Mock
    private ImportMatcher matcher;
    @Mock
    private IOUtils ioUtil;
    @InjectMocks
    private SourceTreeAnalyzerImpl subject;

    private Path javaFile1;
    private Path javaFile2;

    private Path root;
    private Stream<Path> rootStream;

    @Before
    public void setUp() throws Exception {
        this.root = mock(Path.class);
        this.rootStream = Stream.of(this.root);

        this.javaFile1 = mockFile("Foo.java");
        this.javaFile2 = mockFile("Bar.java");

    }

    @Test
    public void testName() throws Exception {
        final ArgumentCaptor<Predicate> filter = ArgumentCaptor.forClass(Predicate.class);
        when(this.ioUtil.listFiles(eq(this.root), filter.capture())).thenReturn(Stream.of(
                this.javaFile1, this.javaFile2));
        final Path sourceFile = mockFile("xyz");

        final Match file1Match = new Match(sourceFile, 1, "dfdg");
        when(this.matcher.matchFile(eq(this.javaFile1), any())).thenReturn(
                Stream.of(file1Match));
        when(this.matcher.matchFile(eq(this.javaFile2), any())).thenReturn(
                Stream.empty());
        final Map<Path, List<Match>> result = this.subject.analyze(
                this.rootStream, mock(BannedImportGroup.class));

        final Match actual = result.get(sourceFile).get(0);
        assertSame(file1Match, actual);
        final Predicate pred = filter.getValue();
        assertTrue(pred.test(this.javaFile1));
        assertTrue(pred.test(this.javaFile2));

        final Path dir = mock(Path.class);
        when(this.ioUtil.isFile(dir)).thenReturn(false);
        assertFalse(pred.test(dir));
    }

    private Path mockFile(String fileName) {
        final Path path = mock(Path.class);
        final Path fn = mock(Path.class);
        when(path.getFileName()).thenReturn(fn);
        when(fn.toString()).thenReturn(fileName);
        when(this.ioUtil.isFile(path)).thenReturn(true);
        return path;
    }

}
