package com.pa.paperless.adapter.node;

import com.chad.library.adapter.base.entity.node.BaseExpandNode;
import com.chad.library.adapter.base.entity.node.BaseNode;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Created by xlk on 2021/1/14.
 * @desc
 */
public class LevelDirNode extends BaseExpandNode {
    private List<BaseNode> childNode;
    int dirId;
    String dirName;

    public LevelDirNode(List<BaseNode> childNode, int dirId, String dirName) {
        this.childNode = childNode;
        this.dirId = dirId;
        this.dirName = dirName;
    }

    public void setChildNode(List<BaseNode> childNode) {
        this.childNode = childNode;
    }

    public int getDirId() {
        return dirId;
    }

    public String getDirName() {
        return dirName;
    }

    @Nullable
    @Override
    public List<BaseNode> getChildNode() {
        return childNode;
    }
}
