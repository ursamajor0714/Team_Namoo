import { useEffect } from 'react'

/**
 * 공용 모달 껍데기. 배경 클릭 또는 Esc 키로 닫힌다.
 * 본문/버튼은 children 으로 넣는다.
 * @param {{ title: string, onClose: () => void, children: React.ReactNode }} props
 */
function Modal({ title, onClose, children }) {
  useEffect(() => {
    function handleKeyDown(event) {
      if (event.key === 'Escape') {
        onClose()
      }
    }
    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [onClose])

  return (
    <div className="modal__backdrop" onClick={onClose}>
      <div
        className="modal"
        role="dialog"
        aria-modal="true"
        aria-label={title}
        onClick={(event) => event.stopPropagation()}
      >
        <h2 className="modal__title">{title}</h2>
        {children}
      </div>
    </div>
  )
}

export default Modal
