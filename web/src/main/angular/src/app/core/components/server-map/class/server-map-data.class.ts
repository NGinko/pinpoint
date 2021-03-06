import { NodeGroup } from './node-group.class';
import { LinkGroup } from './link-group.class';
import { MultiConnectNodeGroup } from './multi-connect-node-group.class';
import { Filter } from 'app/core/models';
interface INodeInfoMap {
    [key: string]: INodeInfo;
}
interface ILinkInfoMap {
    [key: string]: ILinkInfo;
}
export interface IShortNodeInfo {
    key: string;
    isWas?: boolean;
    isQueue?: boolean;
    category: string;
    hasAlert?: boolean;
    slowCount?: number;
    histogram?: IResponseTime | IResponseMilliSecondTime;
    errorCount?: number;
    totalCount?: number;
    serviceType: string;
    isAuthorized: boolean;
    instanceCount: number;
    applicationName: string;

    mergedNodes?: any[];
    topCountNodes?: any[];

    mergedSourceNodes?: any[];

    agentHistogram?: { [key: string]: IResponseTime | IResponseMilliSecondTime }[];
    agentTimeSeriesHistogram?: { [key: string]: IHistogram[] };
    agentIds?: string[];
    instanceErrorCount?: number;
    serverList?: { [key: string]: IServerInfo };
    serviceTypeCode?: string;
    timeSeriesHistogram?: IHistogram[];
}
export interface IShortLinkInfo {
    to: string;
    key: string;
    from: string;
    hasAlert: boolean;
    slowCount: number;
    targetInfo: ISourceInfo;
    totalCount: number;
    sourceInfo: ISourceInfo;
    errorCount: number;

    filterApplicationName?: string;
    filterApplicationServiceTypeCode?: number;
    filterApplicationServiceTypeName?: string;
    filterTargetRpcList?: any[];
    fromAgent?: string[];
    histogram?: IResponseTime | IResponseMilliSecondTime;
    sourceHistogram?: { [key: string]: IResponseTime | IResponseMilliSecondTime };
    sourceTimeSeriesHistogram?: { [key: string]: IHistogram }[];
    targetHistogram?: { [key: string]: IResponseTime | IResponseMilliSecondTime };
    timeSeriesHistogram?: IHistogram[];
    toAgent?: string[];
}
interface IShortNodeInfoMap {
    [key: string]: IShortNodeInfo;
}
interface IShortLinkInfoMap {
    [key: string]: IShortLinkInfo;
}
interface IStateCheckMap {
    [key: string]: boolean;
}

interface ILinkCount {
    inCount: number;
    outCount: number;
    loopCount: number;
}
export class ServerMapData {
    private nodeList: IShortNodeInfo[];
    private linkList: IShortLinkInfo[];
    private mergeableServiceType: IStateCheckMap;
    private canNotMergeServiceTypeList = ['USER'];
    private countMap: { [key: string]: ILinkCount };
    private nodeMap: IShortNodeInfoMap;
    private linkMap: IShortLinkInfoMap;
    private mergeStateMap: IStateCheckMap = {};
    private groupServiceTypeMap: IStateCheckMap;
    private originalNodeMap: INodeInfoMap = {};
    private originalLinkMap: ILinkInfoMap = {};

    constructor(
        private originalNodeList: INodeInfo[],
        private originalLinkList: ILinkInfo[],
        private filters?: Filter[]
    ) {
        this.init();
    }
    reset(originalNodeList: INodeInfo[], originalLinkList: ILinkInfo[]) {
        this.originalNodeList = originalNodeList;
        this.originalLinkList = originalLinkList;
        this.init();
    }
    private initVar(): void {
        this.nodeList = [];
        this.linkList = [];
        this.mergeableServiceType = {};
        this.countMap = {};
        this.nodeMap = {};
        this.linkMap = {};
        this.groupServiceTypeMap = {};
    }
    private init() {
        this.initVar();
        this.convertToMap();
        this.extractNodeData();
        this.extractLinkData();
        this.extractLinkCountData();
        this.extractServiceTypeWhichCanMerge();
        // option set merge
        // if ( showMergedStatus ) {
        this.mergeNodes();
        this.mergeMultiLinkNodes();
        this.addFilterFlag();
        // }
    }
    // list ???????????? ???????????? map ????????? ?????????.
    private convertToMap(): void {
        // console.time('convertTimeToMapFromList');
        this.originalNodeList.forEach((value: INodeInfo) => {
            this.originalNodeMap[value.key] = value;
        });
        this.originalLinkList.forEach((value: ILinkInfo) => {
            this.originalLinkMap[value.key] = value;
        });
        // console.timeEnd('convertTimeToMapFromList');
    }
    // ?????? ?????? ???????????? ?????? server-map ??? ?????? ??? ?????? ??? ???????????? ?????? ??? ????????? node list??? ????????? ???.
    private extractNodeData(): void {
        this.originalNodeList.forEach((node: INodeInfo) => {
            const oNewNode: IShortNodeInfo = {
                key: node.key,
                isWas: node.isWas,
                isQueue: node.isQueue,
                category: node.category,
                hasAlert: node.hasAlert,
                slowCount: node.slowCount,
                histogram: node.histogram,
                errorCount: node.errorCount,
                totalCount: node.totalCount,
                serviceType: node.serviceType,
                isAuthorized: node.isAuthorized,
                instanceCount: node.instanceCount,
                serviceTypeCode: node.serviceTypeCode,
                applicationName: node.applicationName
            };
            this.nodeList.push(oNewNode);
            this.nodeMap[node.key] = oNewNode;
        });
    }
    // ?????? ?????? ???????????? ?????? server-map ??? ?????? ??? ?????? ??? ???????????? ?????? ??? ????????? link list??? ????????? ???.
    private extractLinkData(): void {
        this.originalLinkList.forEach((link: ILinkInfo) => {
            const oNewLink: IShortLinkInfo = {
                to: link.to,
                key: link.key,
                from: link.from,
                hasAlert: link.hasAlert,
                slowCount: link.slowCount,
                targetInfo: link.targetInfo,
                totalCount: link.totalCount,
                sourceInfo: link.sourceInfo,
                errorCount: link.errorCount
            };
            this.linkList.push(oNewLink);
            this.linkMap[link.key] = oNewLink;
        });
    }
    private extractLinkCountData(): void {
        this.nodeList.forEach((node: IShortNodeInfo) => {
            this.countMap[node.key] = {
                inCount: 0,
                outCount: 0,
                loopCount: 0
            };
        });
        this.linkList.forEach((link: IShortLinkInfo) => {
            this.countMap[link.to].inCount++;
            this.countMap[link.from].outCount++;
            if (typeof this.linkMap[link.to + '~' + link.from] !== 'undefined') {
                this.countMap[link.to].loopCount++;
            }
        });
    }
    // ?????? ????????? ?????? ??? ?????? serviceType??? ??????.
    private extractServiceTypeWhichCanMerge() {
        this.nodeList.forEach((node) => {
            if (this.canMergeType(node)) {
                this.mergeableServiceType[node.serviceType] = true;
            }
        });
    }
    private canMergeType(nodeData: any): boolean {
        return nodeData.isWas === false && this.canNotMergeServiceTypeList.indexOf(nodeData.serviceType) === -1;
    }
    private isLeafNode(key: string) {
        return (this.countMap[key].outCount - this.countMap[key].loopCount) === 0;
    }
    private mergeNodes(): void {
        // console.time('mergeGroup()');
        const collectMergeLink: {[key: string]: any} = {};
        const removeNodeKeys: IStateCheckMap = {};
        const removeLinkKeys: IStateCheckMap = {};
        // ??? ????????? ???????????? ?????? ????????? ????????? ?????? ???????????? ????????????
        // ????????? ???????????? ?????? [from.serviceType] ?????? ????????? ??????.
        // ?????? ??? ????????? ????????? ??????
        this.linkList.forEach((link) => {
            if (this.hasMergeableNode(link) === false) {
                return;
            }
            if ((link.from in collectMergeLink) === false) {
                collectMergeLink[link.from] = {};
            }
            if ((link.targetInfo.serviceType in collectMergeLink[link.from]) === false) {
                collectMergeLink[link.from][link.targetInfo.serviceType] = [];
            }
            /**
             *  * collectMergeLink = { ACL-PORTAL-DEV(fromNode Key): { UNKNOWN: [link1, link2, link3] } } ???????????????.
             */
            collectMergeLink[link.from][link.targetInfo.serviceType].push(link);
        });
        for (const nodeKey in collectMergeLink) {
            if (collectMergeLink[nodeKey]) {
                for (const type in collectMergeLink[nodeKey]) {
                    if (collectMergeLink[nodeKey][type].length < 2 || this.mergeStateMap[type] === false) {
                        continue;
                    }
                    const nodeGroup = new NodeGroup(type);
                    const linkGroup = new LinkGroup(nodeKey, nodeGroup.getGroupKey());
                    const collectLoopLink: IShortLinkInfo[] = [];

                    // ????????? ????????? ?????? ????????? ????????? ???????????? ?????? ????????? ??????
                    // loop ????????? ????????? ?????????.
                    collectMergeLink[nodeKey][type].forEach((link: any) => {
                        nodeGroup.addNodeData(this.nodeMap[link.to]);
                        linkGroup.addLinkData(link);
                        removeNodeKeys[link.to] = true;
                        removeLinkKeys[link.key] = true;
                        if (this.linkMap[link.to + '~' + link.from]) {
                            collectLoopLink.push(this.linkMap[link.to + '~' + link.from]);
                        }
                    });
                    const hasLoopLink = collectLoopLink.length > 0;
                    if (hasLoopLink) {
                        // loop ????????? ????????? ????????? ????????? ????????? ????????? ????????? ???????????? ????????? ?????? ?????? ????????? ??????
                        const loopLinkGroup = new LinkGroup(nodeGroup.getGroupKey(), nodeKey);
                        collectLoopLink.forEach((link: any) => {
                            loopLinkGroup.addLinkData(link);
                            removeLinkKeys[link.key] = true;
                        });
                        this.addNewLink(loopLinkGroup.sortLinkData().getLinkGroupData());
                    }
                    this.countMap[nodeGroup.getGroupKey()] = {
                        inCount: 1,
                        outCount: hasLoopLink ? 1 : 0,
                        loopCount: hasLoopLink ? 1 : 0
                    };

                    // ???????????? ????????? ???????????? ????????? ????????? ???????????? ?????? ??????, ????????? ??????.
                    this.addNewNode(nodeGroup.sortNodeData().getNodeGroupData());
                    this.addNewLink(linkGroup.sortLinkData().getLinkGroupData());
                    this.groupServiceTypeMap[nodeGroup.getGroupServiceType()] = true;
                    this.mergeStateMap[type] = true;
                }
            }
        }
        // ??????????????? ??????, ????????? ??????
        this.removeByKey(this.nodeList, this.nodeMap, removeNodeKeys);
        this.removeByKey(this.linkList, this.linkMap, removeLinkKeys);
        // console.timeEnd('mergeGroup()');
    }
    private hasMergeableNode(link: any): boolean {
        if (this.mergeableServiceType[link.targetInfo.serviceType] !== true) {
            return false;
        }
        if (this.countMap[link.to].inCount !== 1) {
            return false;
        }
        if (this.isLeafNode(link.to) === false) {
            return false;
        }
        if (this.filters) {
            return !this.filters.some(({toApplication}: Filter) => link.targetInfo.applicationName === toApplication);
        }
        return true;
    }
    private addNewNode(newNode: IShortNodeInfo): void {
        this.nodeList.push(newNode);
        this.nodeMap[newNode.key] = newNode;
    }
    private addNewLink(newLink: any): void {
        this.linkList.push(newLink);
        this.linkMap[newLink.key] = newLink;
    }
    setMergeState({name, state}: IServerMapMergeState): void {
        this.mergeStateMap[name] = state;
    }
    resetMergeState(): void {
        this.initVar();
        this.extractNodeData();
        this.extractLinkData();
        this.extractLinkCountData();
        this.extractServiceTypeWhichCanMerge();
        this.mergeNodes();
        this.mergeMultiLinkNodes();
        this.addFilterFlag();
    }
    removeByKey(dataList: any, dataMap: any, removeList: any) {
        const removeIndex: Array<number> = [];
        dataList.forEach((thing: any, index: number) => {
            if (removeList[thing.key] === true) {
                delete dataMap[thing.key];
                removeIndex.push(index);
            }
        });
        removeIndex.sort(function (v1, v2) {
            return v1 - v2;
        });
        for (let i = removeIndex.length - 1; i >= 0; i--) {
            dataList.splice(removeIndex[i], 1);
        }
    }
    mergeMultiLinkNodes(): void {
        // console.time('mergeMultiLinkGroup()');
        // [1] ?????? ????????? ?????? ????????? ???????????? ???????????? ??????
        // *: Node??? ????????? ??????????????????, ??????????????? 2???????????????. ????????? ?????? ????????????????????? ?????????????????????.
        const targetNodeList = this.getMergeTargetNodes();
        const checkedNodes: IStateCheckMap = {};
        const removeNodeKeys: IStateCheckMap = {};
        const removeLinkKeys: IStateCheckMap = {};
        if (targetNodeList.length <= 1) {
            // console.timeEnd('mergeMultiLinkGroup()');
            return;
        }

        targetNodeList.forEach((outerNode: IShortNodeInfo) => {
            if (this.mergeStateMap[outerNode.serviceType] === false) {
                return;
            }
            const outerNodeKey = outerNode.key;
            // inner loop ??? ????????? ????????? ?????? ???????????? ?????? ??????
            if (checkedNodes[outerNodeKey] === true) {
                return;
            }
            checkedNodes[outerNodeKey] = true;
            const fromNodeKeysOfOuter: string[] = this.getFromNodeKeys(outerNodeKey); // * ?????? ???????????? ?????????????????? fromNode???
            const mergeTargetLinks: {[key: string]: IShortLinkInfo[] } = {};
            const mergeTargetLoopLinks: {[key: string]: IShortLinkInfo[] } = {};
            const mergeTargetNodeList: IShortNodeInfo[] = [];

            targetNodeList.forEach((innerNode: IShortNodeInfo) => {
                const innerNodeKey = innerNode.key;
                if (checkedNodes[innerNodeKey] === true) {
                    return;
                }
                if (outerNode.serviceType !== innerNode.serviceType) {
                    return;
                }
                checkedNodes[innerNodeKey] = true;
                const fromNodeKeysOfInner: string[] = this.getFromNodeKeys(innerNodeKey);
                // [2] ??? ?????? ?????? ?????? ????????? from node ??? ????????? ??????
                if (this.hasSameNodeList(fromNodeKeysOfOuter, fromNodeKeysOfInner) === false) {
                    return;
                }
                this.extractConnectLink(mergeTargetLinks, mergeTargetLoopLinks, fromNodeKeysOfInner, innerNodeKey);
                mergeTargetNodeList.push(innerNode);
            });
            if (mergeTargetNodeList.length > 0) {
                this.extractConnectLink(mergeTargetLinks, mergeTargetLoopLinks, fromNodeKeysOfOuter, outerNodeKey);
                mergeTargetNodeList.push(outerNode);

                const multiConnectNodeGroup = new MultiConnectNodeGroup(outerNode.serviceType);
                // ????????? ???????????? ????????? ?????? ????????? ?????????.
                mergeTargetNodeList.forEach((node: IShortNodeInfo) => {
                    multiConnectNodeGroup.addNodeData(node);
                    removeNodeKeys[node.key] = true;
                });

                for (const fromKey in mergeTargetLinks) {
                    if (mergeTargetLinks.hasOwnProperty(fromKey)) {
                        multiConnectNodeGroup.addSubNodeGroup(fromKey);
                        const linkGroup = new LinkGroup(fromKey, multiConnectNodeGroup.getGroupKey());
                        for (let i = 0; i < mergeTargetLinks[fromKey].length; i++) {
                            const link = mergeTargetLinks[fromKey][i];
                            multiConnectNodeGroup.addSubNodeGroupData(fromKey, link);
                            linkGroup.addLinkData(link);
                            removeLinkKeys[link.key] = true;
                        }
                        this.linkList.push(linkGroup.sortLinkData().getLinkGroupData());
                    }
                }
                for (const fromKey in mergeTargetLoopLinks) {
                    if (mergeTargetLoopLinks.hasOwnProperty(fromKey)) {
                        // multiConnectNodeGroup.addSubNodeGroup(multiConnectNodeGroup.getGroupKey());
                        const linkGroup = new LinkGroup(multiConnectNodeGroup.getGroupKey(), fromKey);
                        for (let i = 0; i < mergeTargetLoopLinks[fromKey].length; i++) {
                            const link = mergeTargetLoopLinks[fromKey][i];
                            // multiConnectNodeGroup.addSubNodeGroupData(multiConnectNodeGroup.getGroupKey(), link);
                            linkGroup.addLinkData(link);
                            removeLinkKeys[link.key] = true;
                        }
                        this.linkList.push(linkGroup.sortLinkData().getLinkGroupData());
                    }
                }
                this.nodeList.push(multiConnectNodeGroup.sortNodeData().getNodeGroupData());
                this.groupServiceTypeMap[multiConnectNodeGroup.getGroupServiceType()] = true;
                this.mergeStateMap[outerNode.serviceType] = true;
            }
        });
        this.removeByKey(this.nodeList, this.nodeMap, removeNodeKeys);
        this.removeByKey(this.linkList, this.linkMap, removeLinkKeys);
        // console.timeEnd('mergeMultiLinkGroup()');
    }
    private extractConnectLink(mergeTargetLinks: {[key: string]: IShortLinkInfo[] }, mergeTargetLoopLinks: {[key: string]: IShortLinkInfo[] }, fromKeys: string[], toKey: string): void {
        fromKeys.forEach((fromKey: string) => {
            if ((fromKey in mergeTargetLinks) === false) {
                mergeTargetLinks[fromKey] = [];
            }
            mergeTargetLinks[fromKey].push(this.linkMap[fromKey + '~' + toKey]);
            // loop ????????? ????????? ????????? ???.
            if (this.linkMap[toKey + '~' + fromKey]) {
                if ((fromKey in mergeTargetLoopLinks) === false) {
                    mergeTargetLoopLinks[fromKey] = [];
                }
                mergeTargetLoopLinks[fromKey].push(this.linkMap[toKey + '~' + fromKey]);
            }
        });
    }
    private getMergeTargetNodes(): IShortNodeInfo[] {
        return this.nodeList.filter((node) => {
            const count = this.countMap[node.key];
            if (count.outCount - count.loopCount > 0) {
                return false;
            }
            if (count.inCount < 2) {
                return false;
            }
            return true;
        });
    }
    private hasSameNodeList(firstNodeList: Array<string>, secondNodeList: Array<string>): boolean {
        if (firstNodeList.length !== secondNodeList.length) {
            return false;
        }
        for (let i = 0; i < firstNodeList.length; i++) {
            if (secondNodeList.indexOf(firstNodeList[i]) === -1) {
                return false;
            }
        }
        return true;
    }
    private getFromNodeKeys(nodeKey: string): string[] {
        return this.linkList.reduce((acc, link) => {
            if (link.to === nodeKey) {
                acc.push(link.from);
            }
            return acc;
        }, []);
    }
    getNodeList(): { [key: string]: any }[] {
        return this.nodeList;
    }
    getLinkList(): { [key: string]: any }[] {
        return this.linkList;
    }
    getGroupTypes(): Array<string> {
        const types: Array<string> = [];
        for (const type in this.groupServiceTypeMap) {
            if (this.groupServiceTypeMap.hasOwnProperty(type)) {
                types.push(type);
            }
        }
        return types;
    }
    getMergeState(): any {
        return Object.keys(this.mergeStateMap).reduce((accumulator: IStateCheckMap, key: string) => {
            accumulator[key] = this.mergeStateMap[key];
            return accumulator;
        }, {});
    }
    getLinkListByFrom(from: string): ILinkInfo[] {
        const linkList: ILinkInfo[] = [];
        Object.keys(this.originalLinkMap).forEach((key: string) => {
            const link = this.originalLinkMap[key];
            if (link.from === from) {
                linkList.push(link);
            }
        });
        return linkList;
    }
    getLinkListByTo(to: string): ILinkInfo[] {
        const linkList: ILinkInfo[] = [];
        Object.keys(this.originalLinkMap).forEach((key: string) => {
            const link = this.originalLinkMap[key];
            if (link.to === to) {
                linkList.push(link);
            }
        });
        return linkList;
    }
    getMergedNodeData(key: string): IShortNodeInfo {
        return this.nodeList.find((node: IShortNodeInfo) => node.key === key);
    }
    getMergedLinkData(key: string): IShortLinkInfo {
        return this.linkList.find((link: IShortLinkInfo) => link.key === key);
    }
    getNodeData(key: string): INodeInfo | IShortNodeInfo {
        return this.originalNodeMap[key] || this.nodeMap[key];
    }
    getLinkData(key: string): ILinkInfo | IShortLinkInfo {
        return this.originalLinkMap[key] || this.linkMap[key];
    }
    addFilterFlag(): void {
        if (this.filters) {
            this.linkList.forEach((link: any) => {
                this.filters.forEach((filter: Filter) => {
                    if (filter.getFromKey() === link.from && filter.getToKey() === link.to) {
                        link['isFiltered'] = true;
                        return;
                    }
                });
            });
        }
    }
    getNodeCount(): number {
        return this.nodeList.length;
    }
}
