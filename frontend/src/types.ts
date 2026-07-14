export type UserRole = 'ADMIN' | 'FARM' | 'PARTNER'

export type AttributeDataType = 'STRING' | 'NUMBER' | 'DATE' | 'ENUM' | 'BOOLEAN'
export type ListingSide = 'OFFER' | 'NEED'
export type LogisticsType = 'PICKUP' | 'DELIVERY' | 'ON_SITE'
export type CategoryStatus = 'ACTIVE' | 'INACTIVE'
export type AllowedSides = 'OFFER' | 'NEED' | 'BOTH'
export type ListType = 'ING' | 'TODAY' | 'CLOSED' | 'ALL'
export type ArticleType = 'NEWS' | 'NOTICE'
export type ProductOrderStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'

export interface AttributeDef {
  id: number
  fieldKey: string
  label: string
  dataType: AttributeDataType
  required: boolean
  enumOptions: string[]
  placeholder?: string
  sortOrder: number
  matchable: boolean
  showInList: boolean
  showInNotify: boolean
}

export interface Category {
  id: number
  code: string
  name: string
  description?: string
  allowedSides: AllowedSides
  defaultUnit?: string
  status: CategoryStatus
  sortOrder: number
  attributes: AttributeDef[]
}

export interface AuthUser {
  token: string
  userId: number
  username: string
  displayName: string
  role: UserRole
  organizationId: number
  organizationName: string
  farmCode?: string | null
  mesLinked: boolean
}

export interface Listing {
  id: number
  categoryId: number
  categoryCode: string
  categoryName: string
  organizationId: number
  organizationName: string
  organizationPhone?: string
  side: ListingSide
  regionCode: string
  quantity: number
  unit: string
  targetPrice?: number
  logisticsType: LogisticsType
  expiresAt?: string
  status: string
  source: string
  title?: string
  memo?: string
  attributes: Record<string, unknown>
  createdAt: string
  featuredUntil?: string | null
  featured: boolean
}

export interface Article {
  id: number
  type: ArticleType
  title: string
  summary?: string
  body: string
  published: boolean
  publishedAt?: string
  createdAt: string
}

export interface PortalProduct {
  id: number
  code: string
  name: string
  description?: string
  priceHint?: number
  durationDays: number
  active: boolean
}

export interface ProductOrder {
  id: number
  productId: number
  productCode: string
  productName: string
  organizationId: number
  organizationName: string
  listingId?: number
  listingTitle?: string
  status: ProductOrderStatus
  memo?: string
  adminMemo?: string
  createdAt: string
  decidedAt?: string
}

export interface Inquiry {
  id: number
  listingId: number
  listingTitle?: string
  fromOrganizationId: number
  fromOrganizationName: string
  message: string
  contactPhone?: string
  status: string
  replyMemo?: string
  createdAt: string
}

export interface Preference {
  id: number
  categoryId: number
  categoryCode: string
  categoryName: string
  regions: string[]
  minQuantity?: number
  attributeFilters: Record<string, unknown>
  pushEnabled: boolean
}

export interface NotificationItem {
  id: number
  title: string
  body: string
  type: string
  refId: number
  readFlag: boolean
  createdAt: string
}
