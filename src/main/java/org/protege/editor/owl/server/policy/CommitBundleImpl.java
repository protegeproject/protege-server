package org.protege.editor.owl.server.policy;

import org.protege.editor.owl.server.api.CommitBundle;
import org.protege.editor.owl.server.versioning.Commit;
import org.protege.editor.owl.server.versioning.api.DocumentRevision;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

public class CommitBundleImpl implements CommitBundle {

    private static final long serialVersionUID = -6145139680901780930L;

    private DocumentRevision headRevision;
    private List<Commit> commits;

    public CommitBundleImpl(DocumentRevision headRevision, List<Commit> multipleCommits) {
        this.headRevision = headRevision;
        this.commits = multipleCommits;
    }

    public CommitBundleImpl(DocumentRevision headRevision, Commit singleCommit) {
        this(headRevision, Lists.newArrayList(singleCommit));
    }

    @Override
    public DocumentRevision getHeadRevision() {
        return headRevision;
    }

    @Override
    public List<Commit> getCommits() {
        return Collections.unmodifiableList(commits);
    }
}
