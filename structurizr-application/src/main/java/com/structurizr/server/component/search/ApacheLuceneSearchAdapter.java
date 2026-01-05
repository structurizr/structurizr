package com.structurizr.server.component.search;

import com.structurizr.Workspace;
import com.structurizr.configuration.Configuration;
import com.structurizr.documentation.Decision;
import com.structurizr.documentation.Documentation;
import com.structurizr.documentation.Section;
import com.structurizr.model.*;
import com.structurizr.util.StringUtils;
import com.structurizr.view.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Implementation of a search component, using Apache Lucene.
 */
class ApacheLuceneSearchAdapter extends AbstractSearchAdapter {

    private static final Log log = LogFactory.getLog(ApacheLuceneSearchAdapter.class);
    private static final String INDEX_DIRECTORY_NAME = "index";

    private static final String URL_KEY = "url";
    private static final String WORKSPACE_KEY = "workspace";
    private static final String NAME_KEY = "name";
    private static final String DESCRIPTION_KEY = "description";
    private static final String TYPE_KEY = "type";
    private static final String CONTENT_KEY = "content";

    private static final String MARKDOWN_SECTION_HEADING = "## ";
    private static final String ASCIIDOC_SECTION_HEADING = "== ";
    private static final String NEWLINE = "\n";

    private final File indexDirectory;
    private IndexWriter indexWriter;

    ApacheLuceneSearchAdapter() {
        this.indexDirectory = new File(Configuration.getInstance().getWorkDirectory(), INDEX_DIRECTORY_NAME);
    }

    @Override
    public void start() {
        FileSystemUtils.deleteRecursively(indexDirectory);
        createIndexDirectory();

        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);

        IndexSearcher.setMaxClauseCount(9999);

        try {
            Directory dir = FSDirectory.open(indexDirectory.toPath());
            indexWriter = new IndexWriter(dir, iwc);
        } catch (IOException e) {
            log.error(e);
        }
    }

    @Override
    public void stop() {
        try {
            if (indexWriter != null) {
                indexWriter.close();
            }
        } catch (IOException e) {
            log.warn(e);
        }
    }

    private void createIndexDirectory() {
        if (!indexDirectory.exists()) {
            try {
                Files.createDirectory(indexDirectory.toPath());
            } catch (IOException e) {
                log.error("Error creating Lucene index directory", e);
            }
        }
    }

    @Override
    public void index(Workspace workspace) {
        try {
            delete(workspace.getId());

            Document doc = new Document();
            doc.add(new StoredField(URL_KEY, ""));
            doc.add(new TextField(WORKSPACE_KEY, toString(workspace.getId()), Field.Store.YES));
            doc.add(new TextField(TYPE_KEY, DocumentType.WORKSPACE, Field.Store.YES));
            doc.add(new StoredField(NAME_KEY, toString(workspace.getName())));
            doc.add(new StoredField(DESCRIPTION_KEY, toString(workspace.getDescription())));
            doc.add(new TextField(CONTENT_KEY, appendAll(workspace.getName(), workspace.getDescription()), Field.Store.NO));
            indexWriter.addDocument(doc);
            indexWriter.commit();

            for (CustomView view : workspace.getViews().getCustomViews()) {
                index(workspace, view, indexWriter);
            }
            for (SystemLandscapeView view : workspace.getViews().getSystemLandscapeViews()) {
                index(workspace, view, indexWriter);
            }
            for (SystemContextView view : workspace.getViews().getSystemContextViews()) {
                index(workspace, view, indexWriter);
            }
            for (ContainerView view : workspace.getViews().getContainerViews()) {
                index(workspace, view, indexWriter);
            }
            for (ComponentView view : workspace.getViews().getComponentViews()) {
                index(workspace, view, indexWriter);
            }
            for (DynamicView view : workspace.getViews().getDynamicViews()) {
                index(workspace, view, indexWriter);
            }
            for (DeploymentView view : workspace.getViews().getDeploymentViews()) {
                index(workspace, view, indexWriter);
            }

            indexDocumentationAndDecisions(workspace, null, workspace.getDocumentation());

            for (SoftwareSystem softwareSystem : workspace.getModel().getSoftwareSystems()) {
                indexDocumentationAndDecisions(workspace, softwareSystem, softwareSystem.getDocumentation());

                for (Container container : softwareSystem.getContainers()) {
                    indexDocumentationAndDecisions(workspace, container, container.getDocumentation());

                    for (Component component : container.getComponents()) {
                        indexDocumentationAndDecisions(workspace, component, component.getDocumentation());
                    }
                }
            }

        } catch (Exception e) {
            log.error(e);
        }
    }

    private void index(Workspace workspace, ModelView view, IndexWriter indexWriter) throws Exception {
        Document doc = new Document();
        doc.add(new StoredField(URL_KEY, DIAGRAMS_PATH + "#" + view.getKey()));
        doc.add(new TextField(WORKSPACE_KEY, toString(workspace.getId()), Field.Store.YES));
        doc.add(new TextField(TYPE_KEY, DocumentType.DIAGRAM, Field.Store.YES));
        doc.add(new StoredField(NAME_KEY, toString(view.getName())));
        doc.add(new StoredField(DESCRIPTION_KEY, toString(view.getDescription())));

        StringBuilder content = new StringBuilder();

        if (!StringUtils.isNullOrEmpty(view.getTitle())) {
            content.append(view.getTitle());
            content.append(" ");
        } else if (!StringUtils.isNullOrEmpty(view.getName())) {
            content.append(view.getName());
            content.append(" ");
        }

        if (!StringUtils.isNullOrEmpty(view.getDescription())) {
            content.append(view.getDescription());
            content.append(" ");
        }

        for (ElementView elementView : view.getElements()) {
            Element element = elementView.getElement();

            if (element instanceof CustomElement || element instanceof Person || element instanceof SoftwareSystem) {
                content.append(indexElementBasics(element));
            }

            if (element instanceof Container) {
                content.append(indexElementBasics(element));
                String technology = ((Container)element).getTechnology();
                if (!StringUtils.isNullOrEmpty(technology)) {
                    content.append(technology);
                    content.append(" ");
                }
            }

            if (element instanceof Component) {
                content.append(indexElementBasics(element));
                String technology = ((Component)element).getTechnology();
                if (!StringUtils.isNullOrEmpty(technology)) {
                    content.append(technology);
                    content.append(" ");
                }
            }

            if (element instanceof DeploymentNode) {
                content.append(index((DeploymentNode)element));
                content.append(" ");
            }
        }

        for (RelationshipView relationshipView : view.getRelationships()) {
            Relationship relationship = relationshipView.getRelationship();
            if (!StringUtils.isNullOrEmpty(relationship.getDescription())) {
                content.append(relationship.getDescription());
                content.append(" ");
            }

            if (!StringUtils.isNullOrEmpty(relationship.getTechnology())) {
                content.append(relationship.getTechnology());
                content.append(" ");
            }
        }

        doc.add(new TextField(CONTENT_KEY, content.toString(), Field.Store.NO));

        indexWriter.addDocument(doc);
        indexWriter.commit();
    }

    private String indexElementBasics(Element element) {
        StringBuilder content = new StringBuilder();

        if (!StringUtils.isNullOrEmpty(element.getName())) {
            content.append(element.getName());
            content.append(" ");
        }
        if (!StringUtils.isNullOrEmpty(element.getDescription())) {
            content.append(element.getDescription());
            content.append(" ");
        }

        return content.toString();
    }

    private String index(DeploymentNode deploymentNode) {
        StringBuilder content = new StringBuilder();

        content.append(indexElementBasics(deploymentNode));
        String technology = deploymentNode.getTechnology();
        if (!StringUtils.isNullOrEmpty(technology)) {
            content.append(technology);
            content.append(" ");
        }

        for (DeploymentNode child : deploymentNode.getChildren()) {
            content.append(index(child));
        }

        for (InfrastructureNode infrastructureNode : deploymentNode.getInfrastructureNodes()) {
            content.append(indexElementBasics(infrastructureNode));
            String infrastructureNodeTechnology = infrastructureNode.getTechnology();
            if (!StringUtils.isNullOrEmpty(infrastructureNodeTechnology)) {
                content.append(infrastructureNodeTechnology);
                content.append(" ");
            }
        }

        for (SoftwareSystemInstance softwareSystemInstance : deploymentNode.getSoftwareSystemInstances()) {
            SoftwareSystem softwareSystem = softwareSystemInstance.getSoftwareSystem();
            content.append(indexElementBasics(softwareSystem));
        }

        for (ContainerInstance containerInstance : deploymentNode.getContainerInstances()) {
            Container container = containerInstance.getContainer();
            content.append(indexElementBasics(container));
            String containerInstanceTechnology = container.getTechnology();
            if (!StringUtils.isNullOrEmpty(containerInstanceTechnology)) {
                content.append(containerInstanceTechnology);
                content.append(" ");
            }
        }

        return content.toString();
    }

    private void indexDocumentationAndDecisions(Workspace workspace, Element element, Documentation documentation) throws Exception {
        if (documentation != null) {
            StringBuilder documentationContent = new StringBuilder();
            for (Section section : documentation.getSections()) {
                documentationContent.append(section.getContent());
                documentationContent.append(NEWLINE);
            }
            indexDocumentation(workspace, element, documentationContent.toString());

            for (Decision decision : documentation.getDecisions()) {
                indexDecision(workspace, element, decision);
            }
        }
    }

    private void indexDocumentation(Workspace workspace, Element element, String documentationContent) throws Exception {
        // split the entire documentation content up into sections, each of which is defined by a ## or == heading.
        String title = "";
        StringBuilder content = new StringBuilder();
        String[] lines = documentationContent.split(NEWLINE);
        int sectionNumber = 0;

        for (String line : lines) {
            if (line.startsWith(MARKDOWN_SECTION_HEADING) || line.startsWith(ASCIIDOC_SECTION_HEADING)) {
                indexDocumentationSection(title, content.toString(), sectionNumber, workspace, element);
                title = line.substring(MARKDOWN_SECTION_HEADING.length()-1).trim();
                content = new StringBuilder();
                sectionNumber++;
            } else {
                content.append(line);
                content.append(NEWLINE);
            }
        }

        if (!content.isEmpty()) {
            indexDocumentationSection(title, content.toString(), sectionNumber, workspace, element);
        }
    }

    private void indexDocumentationSection(String title, String content, int sectionNumber, Workspace workspace, Element element) throws Exception {
        Document doc = new Document();

        doc.add(new StoredField(URL_KEY, DOCUMENTATION_PATH + calculateUrlForSection(element, sectionNumber)));
        doc.add(new TextField(WORKSPACE_KEY, toString(workspace.getId()), Field.Store.YES));
        doc.add(new TextField(TYPE_KEY, DocumentType.DOCUMENTATION, Field.Store.YES));
        if (element == null) {
            if (!StringUtils.isNullOrEmpty(title)) {
                doc.add(new StoredField(NAME_KEY, workspace.getName() + " - " + title));
            } else {
                doc.add(new StoredField(NAME_KEY, workspace.getName()));
            }
        } else {
            if (!StringUtils.isNullOrEmpty(title)) {
                doc.add(new StoredField(NAME_KEY, element.getName() + " - " + title));
            } else {
                doc.add(new StoredField(NAME_KEY, element.getName()));
            }
        }
        doc.add(new StoredField(DESCRIPTION_KEY, ""));
        doc.add(new TextField(CONTENT_KEY, appendAll(title, content), Field.Store.NO));
        indexWriter.addDocument(doc);
        indexWriter.commit();
    }

    private void indexDecision(Workspace workspace, Element element, Decision decision) throws Exception {
        Document doc = new Document();

        doc.add(new StoredField(URL_KEY, DECISIONS_PATH + calculateUrlForDecision(element, decision)));
        doc.add(new TextField(WORKSPACE_KEY, toString(workspace.getId()), Field.Store.YES));
        doc.add(new TextField(TYPE_KEY, DocumentType.DECISION, Field.Store.YES));

        if (element == null) {
            doc.add(new StoredField(NAME_KEY, workspace.getName() + " - " + decision.getId() + ". " + decision.getTitle()));
        } else {
            doc.add(new StoredField(NAME_KEY, element.getName() + " - " + decision.getId() + ". " + decision.getTitle()));
        }

        doc.add(new StoredField(DESCRIPTION_KEY, decision.getStatus()));
        doc.add(new TextField(CONTENT_KEY, appendAll(decision.getTitle(), decision.getContent(), decision.getStatus()), Field.Store.NO));
        indexWriter.addDocument(doc);
        indexWriter.commit();
    }

    private String appendAll(String... strings) {
        StringBuilder buf = new StringBuilder();

        for (String s : strings) {
            if (!StringUtils.isNullOrEmpty(s)) {
                buf.append(s);
                buf.append(" ");
            }
        }

        return buf.toString();
    }

    @Override
    public List<SearchResult> search(String query, String type, Set<Long> workspaceIds) {
        if (workspaceIds.isEmpty()) {
            throw new IllegalArgumentException("One or more workspace IDs must be provided.");
        }

        List<SearchResult> results = new ArrayList<>();

        try {
            BooleanQuery.Builder workspaceQueryBuilder = new BooleanQuery.Builder();
            for (Long workspaceId : workspaceIds) {
                workspaceQueryBuilder.add(new TermQuery(new Term(WORKSPACE_KEY, toString(workspaceId))), BooleanClause.Occur.SHOULD);
            }

            StandardAnalyzer analyzer = new StandardAnalyzer();
            QueryParser qp = new QueryParser(CONTENT_KEY, analyzer);
            qp.setDefaultOperator(QueryParser.Operator.AND);

            BooleanQuery.Builder queryBuilder = new BooleanQuery.Builder()
                    .add(qp.parse(query), BooleanClause.Occur.MUST)
                    .add(workspaceQueryBuilder.build(), BooleanClause.Occur.MUST);

            if (!StringUtils.isNullOrEmpty(type)) {
                queryBuilder.add(new TermQuery(new Term(TYPE_KEY, type)), BooleanClause.Occur.MUST);
            }

            IndexReader reader = DirectoryReader.open(FSDirectory.open(indexDirectory.toPath()));
            IndexSearcher searcher = new IndexSearcher(reader);

            TopDocs topDocs = searcher.search(queryBuilder.build(), 20);

            List<Document> documents = new ArrayList<>();
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                StoredFields storedFields = searcher.storedFields();
                Document doc = storedFields.document(scoreDoc.doc);
                documents.add(doc);
            }

            for (Document doc : documents) {
                SearchResult result = new SearchResult(
                        Long.parseLong(doc.get(WORKSPACE_KEY)),
                        doc.get(URL_KEY),
                        doc.get(NAME_KEY),
                        doc.get(DESCRIPTION_KEY),
                        doc.get(TYPE_KEY)
                );
                results.add(result);
            }
        } catch (Exception e) {
            log.error(e);
        }

        return results;
    }

    @Override
    public void delete(long workspaceId) {
        try {
            Term workspaceIdTerm = new Term(WORKSPACE_KEY, toString(workspaceId));
            indexWriter.deleteDocuments(workspaceIdTerm);
        } catch (Exception e) {
            log.error(e);
        }
    }

}