import { useEffect, useState, type FormEvent } from 'react'
import { api } from '../api'
import type { AllowedSides, AttributeDataType, Category, CategoryStatus } from '../types'

export function AdminCategoriesPage() {
  const [categories, setCategories] = useState<Category[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [newCat, setNewCat] = useState({
    code: '',
    name: '',
    description: '',
    allowedSides: 'BOTH' as AllowedSides,
    defaultUnit: '',
    sortOrder: 10,
  })
  const [newAttr, setNewAttr] = useState({
    fieldKey: '',
    label: '',
    dataType: 'STRING' as AttributeDataType,
    required: false,
    enumOptions: '',
    sortOrder: 1,
    matchable: false,
    showInList: true,
    showInNotify: true,
  })

  async function load() {
    const list = await api<Category[]>('/api/admin/categories')
    setCategories(list)
    if (!selectedId && list[0]) setSelectedId(list[0].id)
  }

  useEffect(() => {
    load().catch((e) => setError(e.message))
  }, [])

  const selected = categories.find((c) => c.id === selectedId) ?? null

  async function createCategory(e: FormEvent) {
    e.preventDefault()
    setError('')
    setMessage('')
    try {
      await api('/api/admin/categories', {
        method: 'POST',
        body: JSON.stringify(newCat),
      })
      setMessage('카테고리가 추가되었습니다. code는 이후 변경할 수 없습니다.')
      setNewCat({
        code: '',
        name: '',
        description: '',
        allowedSides: 'BOTH',
        defaultUnit: '',
        sortOrder: 10,
      })
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : '실패')
    }
  }

  async function toggleStatus() {
    if (!selected) return
    const nextStatus: CategoryStatus = selected.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
    await api(`/api/admin/categories/${selected.id}`, {
      method: 'PUT',
      body: JSON.stringify({
        name: selected.name,
        description: selected.description,
        allowedSides: selected.allowedSides,
        defaultUnit: selected.defaultUnit,
        status: nextStatus,
        sortOrder: selected.sortOrder,
      }),
    })
    setMessage(`상태: ${nextStatus}`)
    await load()
  }

  async function addAttribute(e: FormEvent) {
    e.preventDefault()
    if (!selected) return
    setError('')
    setMessage('')
    try {
      await api(`/api/admin/categories/${selected.id}/attributes`, {
        method: 'POST',
        body: JSON.stringify({
          ...newAttr,
          enumOptions:
            newAttr.dataType === 'ENUM'
              ? newAttr.enumOptions
                  .split(',')
                  .map((s) => s.trim())
                  .filter(Boolean)
              : [],
        }),
      })
      setMessage('속성 필드가 추가되었습니다.')
      setNewAttr({
        fieldKey: '',
        label: '',
        dataType: 'STRING',
        required: false,
        enumOptions: '',
        sortOrder: 1,
        matchable: false,
        showInList: true,
        showInNotify: true,
      })
      await load()
    } catch (err) {
      setError(err instanceof Error ? err.message : '실패')
    }
  }

  return (
    <div className="stack">
      <header className="page-header">
        <div>
          <h1>카테고리 관리</h1>
          <p className="muted">운영자가 카테고리와 속성 스키마를 추가·비활성화합니다.</p>
        </div>
      </header>
      {message && <p className="success">{message}</p>}
      {error && <p className="error">{error}</p>}

      <div className="two-col">
        <section className="panel">
          <h2>카테고리 목록</h2>
          <div className="list compact">
            {categories.map((c) => (
              <button
                key={c.id}
                type="button"
                className={`list-select ${selectedId === c.id ? 'active' : ''}`}
                onClick={() => setSelectedId(c.id)}
              >
                <strong>
                  {c.name} <span className="muted">({c.code})</span>
                </strong>
                <span className="muted">{c.status}</span>
              </button>
            ))}
          </div>
          {selected && (
            <button type="button" className="ghost" onClick={toggleStatus}>
              {selected.status === 'ACTIVE' ? '비활성화' : '활성화'}
            </button>
          )}
        </section>

        <section className="panel">
          <h2>카테고리 추가</h2>
          <form className="form-panel" onSubmit={createCategory}>
            <div className="field-grid">
              <label className="field">
                <span>코드 (불변)</span>
                <input
                  value={newCat.code}
                  onChange={(e) => setNewCat({ ...newCat, code: e.target.value.toUpperCase() })}
                  required
                />
              </label>
              <label className="field">
                <span>이름</span>
                <input
                  value={newCat.name}
                  onChange={(e) => setNewCat({ ...newCat, name: e.target.value })}
                  required
                />
              </label>
              <label className="field">
                <span>허용 Side</span>
                <select
                  value={newCat.allowedSides}
                  onChange={(e) =>
                    setNewCat({ ...newCat, allowedSides: e.target.value as AllowedSides })
                  }
                >
                  <option value="BOTH">BOTH</option>
                  <option value="OFFER">OFFER</option>
                  <option value="NEED">NEED</option>
                </select>
              </label>
              <label className="field">
                <span>기본 단위</span>
                <input
                  value={newCat.defaultUnit}
                  onChange={(e) => setNewCat({ ...newCat, defaultUnit: e.target.value })}
                />
              </label>
            </div>
            <label className="field">
              <span>설명</span>
              <textarea
                value={newCat.description}
                onChange={(e) => setNewCat({ ...newCat, description: e.target.value })}
                rows={2}
              />
            </label>
            <button type="submit">추가</button>
          </form>
        </section>
      </div>

      {selected && (
        <section className="panel">
          <h2>
            {selected.name} 속성 필드 ({selected.attributes.length})
          </h2>
          <div className="list compact">
            {selected.attributes.map((a) => (
              <div key={a.id} className="listing-row">
                <div>
                  <strong>
                    {a.label} <span className="muted">[{a.fieldKey}]</span>
                  </strong>
                  <p className="muted">
                    {a.dataType} · 필수 {a.required ? 'Y' : 'N'} · 매칭 {a.matchable ? 'Y' : 'N'}
                  </p>
                </div>
              </div>
            ))}
          </div>
          <form className="form-panel" onSubmit={addAttribute}>
            <h3>속성 추가</h3>
            <div className="field-grid">
              <label className="field">
                <span>fieldKey</span>
                <input
                  value={newAttr.fieldKey}
                  onChange={(e) => setNewAttr({ ...newAttr, fieldKey: e.target.value })}
                  required
                />
              </label>
              <label className="field">
                <span>라벨</span>
                <input
                  value={newAttr.label}
                  onChange={(e) => setNewAttr({ ...newAttr, label: e.target.value })}
                  required
                />
              </label>
              <label className="field">
                <span>타입</span>
                <select
                  value={newAttr.dataType}
                  onChange={(e) =>
                    setNewAttr({ ...newAttr, dataType: e.target.value as AttributeDataType })
                  }
                >
                  <option value="STRING">STRING</option>
                  <option value="NUMBER">NUMBER</option>
                  <option value="DATE">DATE</option>
                  <option value="ENUM">ENUM</option>
                  <option value="BOOLEAN">BOOLEAN</option>
                </select>
              </label>
              {newAttr.dataType === 'ENUM' && (
                <label className="field">
                  <span>옵션 (쉼표)</span>
                  <input
                    value={newAttr.enumOptions}
                    onChange={(e) => setNewAttr({ ...newAttr, enumOptions: e.target.value })}
                  />
                </label>
              )}
            </div>
            <div className="chip-group">
              <label className="chip">
                <input
                  type="checkbox"
                  checked={newAttr.required}
                  onChange={(e) => setNewAttr({ ...newAttr, required: e.target.checked })}
                />
                필수
              </label>
              <label className="chip">
                <input
                  type="checkbox"
                  checked={newAttr.matchable}
                  onChange={(e) => setNewAttr({ ...newAttr, matchable: e.target.checked })}
                />
                매칭
              </label>
              <label className="chip">
                <input
                  type="checkbox"
                  checked={newAttr.showInNotify}
                  onChange={(e) => setNewAttr({ ...newAttr, showInNotify: e.target.checked })}
                />
                알림노출
              </label>
            </div>
            <button type="submit">속성 추가</button>
          </form>
        </section>
      )}
    </div>
  )
}
