import { useMemo } from 'react'
import type { AttributeDef } from '../types'

interface Props {
  defs: AttributeDef[]
  values: Record<string, unknown>
  onChange: (next: Record<string, unknown>) => void
}

export function DynamicAttributeFields({ defs, values, onChange }: Props) {
  const sorted = useMemo(
    () => [...defs].sort((a, b) => a.sortOrder - b.sortOrder),
    [defs],
  )

  function setField(key: string, value: unknown) {
    onChange({ ...values, [key]: value })
  }

  return (
    <div className="field-grid">
      {sorted.map((def) => (
        <label key={def.fieldKey} className="field">
          <span>
            {def.label}
            {def.required ? ' *' : ''}
          </span>
          {def.dataType === 'ENUM' ? (
            <select
              value={String(values[def.fieldKey] ?? '')}
              onChange={(e) => setField(def.fieldKey, e.target.value)}
              required={def.required}
            >
              <option value="">선택</option>
              {(def.enumOptions ?? []).map((opt) => (
                <option key={opt} value={opt}>
                  {opt}
                </option>
              ))}
            </select>
          ) : def.dataType === 'BOOLEAN' ? (
            <select
              value={String(values[def.fieldKey] ?? '')}
              onChange={(e) => setField(def.fieldKey, e.target.value === 'true')}
              required={def.required}
            >
              <option value="">선택</option>
              <option value="true">예</option>
              <option value="false">아니오</option>
            </select>
          ) : (
            <input
              type={
                def.dataType === 'NUMBER'
                  ? 'number'
                  : def.dataType === 'DATE'
                    ? 'date'
                    : 'text'
              }
              value={String(values[def.fieldKey] ?? '')}
              placeholder={def.placeholder ?? ''}
              onChange={(e) =>
                setField(
                  def.fieldKey,
                  def.dataType === 'NUMBER'
                    ? e.target.value === ''
                      ? ''
                      : Number(e.target.value)
                    : e.target.value,
                )
              }
              required={def.required}
            />
          )}
        </label>
      ))}
    </div>
  )
}
