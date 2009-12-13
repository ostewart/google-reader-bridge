package com.trailmagic.blogbridge.googlereader;

import com.salas.bb.domain.IArticle;
import com.salas.bb.domain.IFeed;
import com.salas.bb.domain.utils.DomainAdapter;

/**
 * Created by: oliver on Date: Dec 10, 2009 Time: 11:30:33 PM
 */
public class GrbDomainAdapter extends DomainAdapter {
    @Override
    public void articleAdded(IFeed feed, IArticle article) {
        article.getLink();
    }

    @Override
    public void propertyChanged(IArticle iArticle, String s, Object o, Object o1) {
        super.propertyChanged(iArticle, s, o, o1);
    }
}
