import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getAdminFeedback, upsertAnswer } from '../../api/endpoints.js'
import AnswerDialog from './AnswerDialog.jsx'

vi.mock('../../api/endpoints.js', () => ({
  getAdminFeedback: vi.fn(),
  upsertAnswer: vi.fn(),
}))

const feedback = { id: 8, title: '로그인 오류' }
const detail = {
  id: 8,
  version: 5,
  content: '로그인 버튼이 동작하지 않습니다.',
  answer: {
    id: 3,
    version: 2,
    content: '기존 답변',
  },
}

describe('AnswerDialog', () => {
  beforeEach(() => {
    getAdminFeedback.mockResolvedValue(detail)
  })

  it('최신 피드백·답변 버전으로 답변을 수정하고 결과 버전을 전달한다', async () => {
    const user = userEvent.setup()
    const onDone = vi.fn()
    upsertAnswer.mockResolvedValue({ feedbackVersion: 6 })
    render(<AnswerDialog feedback={feedback} onDone={onDone} onCancel={vi.fn()} />)

    const textarea = await screen.findByDisplayValue('기존 답변')
    await user.clear(textarea)
    await user.type(textarea, '수정한 답변')
    await user.click(screen.getByRole('button', { name: '답변 수정' }))

    expect(upsertAnswer).toHaveBeenCalledWith(8, {
      feedbackVersion: 5,
      answerVersion: 2,
      content: '수정한 답변',
      markDone: true,
    })
    await waitFor(() => expect(onDone).toHaveBeenCalledWith({ markedDone: true, version: 6 }))
  })

  it('버전 충돌 메시지를 표시하고 창을 유지한다', async () => {
    const user = userEvent.setup()
    const onDone = vi.fn()
    upsertAnswer.mockRejectedValue(new Error('최신 내용을 확인한 뒤 다시 시도해 주세요.'))
    render(<AnswerDialog feedback={feedback} onDone={onDone} onCancel={vi.fn()} />)

    await screen.findByDisplayValue('기존 답변')
    await user.click(screen.getByRole('button', { name: '답변 수정' }))

    expect(await screen.findByText('최신 내용을 확인한 뒤 다시 시도해 주세요.')).toBeInTheDocument()
    expect(onDone).not.toHaveBeenCalled()
    expect(screen.getByRole('button', { name: '답변 수정' })).toBeEnabled()
  })
})
