
import java.lang.Exception;

import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Indexer {

	public static void main(String[] args) throws Exception {
		if(args.length != 2) {
			throw new IllegalArgumentException("Usage: java " + 
				Indexer.class.getName() + " <index dir> <data dir>");
		}

		String indexDir = args[0];
		String dataDir = args[1];

		long start = System.currentTimeMillis();
		Indexer indexer = new Indexer(indexDir);
		int numIndexed;
		try {
			numIndexed = indexer.index(dataDir, new TextFilesFilter());
		} finally {
			indexer.close();
		}

		long end = System.currentTimeMillis();

		System.out.println("Indexing " + numIndexed + " files took " +
			(double)((end-start)/1000.0) + " seconds");
	}

	private IndexWriter writer;

	public Indexer(String indexDir) throws IOException {
		Directory dir = FSDirectory.open(new File(indexDir));
		writer = new IndexWriter(dir,
			new StandardAnalyzer(
				Version.LUCENE_30),
			true,
			IndexWriter.MaxFieldLength.UNLIMITED);
	}

	public void close() throws IOException {
		writer.close();
	}

	public int index(String dataDir, FileFilter filter) 
		throws Exception {

		List<File> files = new ArrayList<File>();
		files.addAll(Arrays.asList(new File(dataDir).listFiles()));
		//File[] files = new File(dataDir).listFiles();

		//for (File f : files) {
		while(files.size() > 0) {
			File f = files.remove(0);

			if (f.isDirectory()) {
				files.addAll(Arrays.asList(f.listFiles()));
			}
			else if (!f.isHidden() &&
				f.exists() &&
				f.canRead() &&
				(filter == null || filter.accept(f))) {
				indexFile(f);
			}

			/*
			if (!f.isDirectory() &&
				!f.isHidden() &&
				f.exists() &&
				f.canRead() &&
				(filter == null || filter.accept(f))) {
				indexFile(f);
			}
			*/
		}

		return writer.numDocs();
	}

	private static class TextFilesFilter implements FileFilter {
		public boolean accept(File path) {
			return path.getName().toLowerCase()
				.endsWith(".txt");
		}
	}

	protected Document getDocument(File f) throws Exception {
		Document doc = new Document();
		doc.add(new Field("contents", new FileReader(f)));
		doc.add(new Field("filename", f.getName(),
			Field.Store.YES, Field.Index.NOT_ANALYZED));
		doc.add(new Field("fullpath", f.getCanonicalPath(),
			Field.Store.YES, Field.Index.NOT_ANALYZED));
		return doc;
	}

	private void indexFile(File f) throws Exception {
		System.out.println("Indexing " + f.getCanonicalPath());
		Document doc = getDocument(f);
		writer.addDocument(doc);
	}
}