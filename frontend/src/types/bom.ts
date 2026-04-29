export interface BOMDTO {
  id?: number
  name: string
  assemblyId: number
  assemblyPartNumber?: string
  status?: string
  versionNumber?: number
  comments?: string
  version?: number
  createdAt?: string
  updatedAt?: string
  createdBy?: string
  updatedBy?: string
}

export interface BOMItemDTO {
  id?: number
  bomId: number
  partRevisionId: number
  partRevisionRevision?: string
  quantity: number
  designator?: string
  findNumber?: number
  isMounted?: boolean
  comment?: string
  scrapFactor?: number
  version?: number
  createdAt?: string
  updatedAt?: string
  createdBy?: string
  updatedBy?: string
}

export interface BOMExplodeNode {
  bomItemId: number
  partRevisionId: number
  partNumber: string
  partName: string
  revision: string
  quantity: number
  scrapFactor?: number
  designator?: string
  findNumber?: number
  isMounted?: boolean
  level: number
  children: BOMExplodeNode[]
}

export interface BOMComparisonResult {
  bom1Id: number
  bom1Name: string
  bom2Id: number
  bom2Name: string
  addedItems: BOMItemDiff[]
  removedItems: BOMItemDiff[]
  modifiedItems: BOMItemChange[]
  totalAdded: number
  totalRemoved: number
  totalModified: number
}

export interface BOMItemDiff {
  partRevisionId: number
  partNumber: string
  partName: string
  revision: string
  quantity: number
  designator?: string
  findNumber?: number
}

export interface BOMItemChange {
  partRevisionId: number
  partNumber: string
  partName: string
  revision: string
  bom1Quantity: number
  bom2Quantity: number
  bom1Designator?: string
  bom2Designator?: string
  bom1FindNumber?: number
  bom2FindNumber?: number
}

export interface BOMSnapshot {
  id: number
  bomId: number
  bomName: string
  snapshotLabel?: string
  comments?: string
  snapshotDate: string
  createdBy: string
  itemCount: number
  totalCost: number
  items: BOMSnapshotItem[]
}

export interface BOMSnapshotItem {
  partRevisionId: number
  partNumber: string
  partName: string
  revision: string
  quantity: number
  unitPrice?: number
  lineCost?: number
  designator?: string
  findNumber?: number
  isMounted?: boolean
  scrapFactor?: number
  comment?: string
}

export interface ComponentDraftDTO {
  id?: number
  ecoId: number
  bomId?: number
  partRevisionId: number
  partRevisionRevision?: string
  quantity: number
  designator?: string
  findNumber?: number
  isMounted?: boolean
  comment?: string
  scrapFactor?: number
  action: 'ADD' | 'REMOVE' | 'MODIFY'
}
