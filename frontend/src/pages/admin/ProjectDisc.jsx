import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { House } from 'lucide-react'
import './project-disc.css'

/**
 * 하단 프로젝트 전환 디스크. 기획 6-1
 *
 * 원형 메뉴의 위쪽 일부만 화면에 노출하고 로고를 호를 따라 배치한다.
 * **슬롯 하나는 홈(전체 현황)** 이며, 프로젝트를 선택해도 각 슬롯의 자리는 바뀌지 않는다.
 *
 * 그 위에 C 가 정한 것 — **팀 확인이 필요한 변경이다.**
 *   평소에는 원이 조금 더 가라앉아 현재 프로젝트만 남고, 마우스를 올리거나
 *   키보드 포커스가 들어오면 짧게 솟아오른다. 본문을 많이 가리지 않으면서도
 *   프로젝트 전환 기능이라는 점은 알아볼 수 있는 높이만 사용한다.
 *
	 * 열린 동안에는 포인터가 원 중심을 가리키는 각도를 따라 부드럽게 기울어, 양끝에서
	 * 일부 가려진 항목을 선택하기 쉽게 안쪽으로 당긴다. 선택 자체는 위치를 바꾸지 않고
	 * 강조 상태만 바꾼다.
 */

/**
 * 로고가 놓이는 반지름(px). 원판의 반지름(CSS 의 --disc-radius = 230)보다 40 작다.
 *
 * **원판 가장자리가 아니라 안쪽에 앉히는 것이 중요하다.** 경계선에 정확히 걸치게 두었더니
 * 로고가 반은 흰 면, 반은 페이지 배경 위에 떠서 미완성으로 보였다.
 */
const RADIUS = 190
/**
 * 슬롯 사이 각도. 호가 휘어 보이는 정도는 반지름이 아니라 이 각도 폭이 정한다.
 * (드롭 ÷ 가로폭 = tan(각도/2) 라 반지름은 약분된다. 반지름은 전체 크기만 바꾼다.)
 *
 * 원판이 작아 호를 따라 잰 간격도 좁다. 로고가 서로 닿지 않으려면 이만큼 벌려야 하고,
 * 그 대가로 세 칸 너머는 화면 아래로 돌아 나간다. 실제 다이얼도 그렇게 돈다.
 */
const STEP = 22
/** 펼쳤을 때 꼭대기 슬롯이 화면 아래 끝에서 뜨는 높이(px). */
const LIFT_OPEN = 124
/** 커서 각도를 회전으로 옮길 때의 비율. 낮게 유지해 움직임이 지나치게 민감하지 않게 한다. */
const TURN_GAIN = 0.3
/** 마우스 움직임만으로 옆 슬롯이 꼭대기를 넘어가지 않도록 한 칸보다 작게 제한한다. */
const TURN_LIMIT = 16
/**
 * 원의 중심이 화면 아래 끝보다 얼마나 밑에 있는지. 펼친 상태 기준이다.
 * 가라앉은 상태는 CSS 의 --disc-sink 가 여기서 더 내리므로 이 파일에는 값이 없다.
 */
const CENTER_DEPTH = RADIUS - LIFT_OPEN
export default function ProjectDisc({ projects, projectCode }) {
	const navigate = useNavigate()
	const [open, setOpen] = useState(false)
	const [turn, setTurn] = useState(0)

	/**
	 * 홈도 슬롯 하나다. 기획 6-1 의 "슬롯 하나는 홈 진입점으로 사용한다".
	 *
	 * **순서는 고정이다.** 로고는 디스크에 붙어 있고, 무엇을 고르든 서로의 좌우 관계가 바뀌지 않는다.
	 * 홈은 목록 한가운데에 둔다. 처음 들어오면 홈이 꼭대기에 있고 프로젝트가 양옆으로 갈라진다.
	 */
	const slots = useMemo(() => {
		const middle = Math.floor(projects.length / 2)
		const toSlot = (project) => ({
			key: project.code,
			name: project.name,
			to: `/admin/${project.code}/board`,
		})

		return [
			...projects.slice(0, middle).map(toSlot),
			{ key: '', name: '전체 현황', to: '/admin', home: true },
			...projects.slice(middle).map(toSlot),
		]
	}, [projects])

	const selectedIndex = Math.max(
		0,
		slots.findIndex((slot) => slot.key === (projectCode ?? '')),
	)
	// 홈을 원판의 고정 기준점으로 둔다. 선택 항목이 바뀌어도 모든 슬롯의 자리는 그대로다.
	const anchorIndex = Math.max(
		0,
		slots.findIndex((slot) => slot.home),
	)

	function close() {
		setOpen(false)
		setTurn(0)
	}

	// pointerover/out 은 자식에서 위로 전달된다. enter/leave 와 달리 바깥으로 나갔는지
	// relatedTarget 으로 직접 확인할 수 있어, 슬롯 사이를 지나갈 때 잘못 닫히지 않는다.
	function handlePointerOut(event) {
		if (!event.currentTarget.contains(event.relatedTarget)) close()
	}

	/**
	 * 포인터가 원 중심에서 이루는 각도를 계산해 원판을 기울인다. 어느 슬롯 위에
	 * 있는지가 아니라 좌표만 사용하므로, 회전한 슬롯이 다시 입력에 영향을 주지 않는다.
	 */
	function handlePointerMove(event) {
		if (!open) return
		const bounds = event.currentTarget.getBoundingClientRect()
		const centerX = bounds.left + bounds.width / 2
		const centerY = bounds.bottom + CENTER_DEPTH
		const degrees =
			(Math.atan2(event.clientX - centerX, centerY - event.clientY) * 180) / Math.PI
		const next = Math.max(-TURN_LIMIT, Math.min(TURN_LIMIT, -degrees * TURN_GAIN))
		// 1도 단위로 끊어 미세한 포인터 떨림마다 다시 그리지 않게 한다.
		setTurn(Math.round(next))
	}

	return (
		<div
			className={open ? 'disc is-open' : 'disc'}
			onPointerOver={() => setOpen(true)}
			onPointerOut={handlePointerOut}
			onPointerMove={handlePointerMove}
			onFocus={() => setOpen(true)}
			onBlur={(event) => {
				if (!event.currentTarget.contains(event.relatedTarget)) close()
			}}
		>
			{/*
			 * 마우스를 감지하는 고정 영역. 원과 슬롯은 떠오르면서 자리를 옮기기 때문에,
			 * 커서 밑에서 빠져나가는 순간 pointerout 이 나 다시 가라앉고, 가라앉으면 또 커서 밑에
			 * 들어와 떠오르는 진동이 생긴다. 움직이지 않는 판을 하나 깔아 그 진동을 끊는다.
			 */}
			<div className="disc__zone" aria-hidden="true" />
			<button
				type="button"
				className="disc__toggle"
				aria-label="프로젝트 선택 열기"
				aria-expanded={open}
				onClick={() => setOpen(true)}
			>
				<span className="disc__cue">PROJECTS</span>
			</button>

			{/* 화면 아래로 잘리는 원. 접혀 있을 때는 조금 더 가라앉는다. */}
			<div className="disc__surface" />

			<nav className="disc__slots" aria-label="프로젝트 전환">
				{slots.map((slot, index) => {
					// 선택과 무관한 고정 위치. 프로젝트를 바꿔도 아이콘 순서가 재배치되지 않는다.
					const offset = index - anchorIndex
					const radian = ((offset * STEP + turn) * Math.PI) / 180

					return (
						<button
							key={slot.to}
							type="button"
							className={index === selectedIndex ? 'disc__slot is-selected' : 'disc__slot'}
							aria-current={index === selectedIndex ? 'page' : undefined}
							style={{
								// 원의 중심은 화면 아래 끝 가운데에서 CENTER_DEPTH 만큼 더 내려간 곳이다.
								transform: `translate(-50%, 50%) translate(${RADIUS * Math.sin(radian)}px, ${-(RADIUS * Math.cos(radian) - CENTER_DEPTH)}px)`,
							}}
							onClick={() => {
								navigate(slot.to)
								close()
							}}
							title={slot.name}
						>
							<span className="disc__logo" data-slot={slot.home ? 'home' : index % 5} aria-hidden="true">
								{slot.home ? <House size={22} strokeWidth={2} /> : slot.name.slice(0, 1)}
							</span>
							<span className="disc__name">{slot.name}</span>
						</button>
					)
				})}
			</nav>
		</div>
	)
}
