import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { House } from 'lucide-react'
import './project-disc.css'

/**
 * 하단 프로젝트 전환 디스크. 기획 6-1
 *
 * 기획대로 "원형 메뉴의 위쪽 일부만 화면에 노출하고, 로고를 호를 따라 배치하고,
 * 클릭으로 회전하며, 최상단에 위치한 프로젝트가 선택된다".
 * **슬롯 하나는 홈(전체 현황)** 이고, 그것도 다른 슬롯과 똑같이 돌아 꼭대기로 온다.
 *
 * 그 위에 C 가 정한 것 — **팀 확인이 필요한 변경이다.**
 *   평소에는 원이 조금 더 가라앉아 봉우리만 남고, 마우스를 올리면 솟아오른다.
 *   보드가 세로로 길어 디스크가 늘 자리를 먹으면 카드가 밀리기 때문이다.
 *   가라앉은 상태에서도 꼭대기와 양옆 이웃은 남으므로 지금 어디인지, 돌릴 수 있다는 것이 드러난다.
 *
 * 로고는 디스크 안에 고정되어 있다. 마우스를 따라 흔들리게 해 봤다가 되돌렸다 —
 * 고르려는 로고가 커서 밑에서 미끄러져 클릭이 어려웠다. 회전은 의도한 입력에만 반응한다.
 *
 * TODO(C, 5단계): 좌우 드래그 회전과 관성 감속, 프로젝트 로고 이미지(logoUrl)를 붙인다.
 *                 지금은 클릭 회전까지다.
 */

/**
 * 로고가 놓이는 반지름(px). 원판의 반지름(CSS 의 --disc-radius = 200)보다 48 작다.
 *
 * **원판 가장자리가 아니라 안쪽에 앉히는 것이 중요하다.** 경계선에 정확히 걸치게 두었더니
 * 로고가 반은 흰 면, 반은 페이지 배경 위에 떠서 미완성으로 보였다.
 */
const RADIUS = 152
/**
 * 슬롯 사이 각도. 호가 휘어 보이는 정도는 반지름이 아니라 이 각도 폭이 정한다.
 * (드롭 ÷ 가로폭 = tan(각도/2) 라 반지름은 약분된다. 반지름은 전체 크기만 바꾼다.)
 *
 * 원판이 작아 호를 따라 잰 간격도 좁다. 로고가 서로 닿지 않으려면 이만큼 벌려야 하고,
 * 그 대가로 세 칸 너머는 화면 아래로 돌아 나간다. 실제 다이얼도 그렇게 돈다.
 */
const STEP = 20
/** 펼쳤을 때 꼭대기 슬롯이 화면 아래 끝에서 뜨는 높이(px). */
const LIFT_OPEN = 92
/**
 * 원의 중심이 화면 아래 끝보다 얼마나 밑에 있는지. 펼친 상태 기준이다.
 * 가라앉은 상태는 CSS 의 --disc-sink 가 여기서 더 내리므로 이 파일에는 값이 없다.
 */
const CENTER_DEPTH = RADIUS - LIFT_OPEN
export default function ProjectDisc({ projects, projectCode }) {
	const navigate = useNavigate()
	const [open, setOpen] = useState(false)

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

	function close() {
		setOpen(false)
	}

	// pointerover/out 은 자식에서 위로 전달된다. enter/leave 와 달리 바깥으로 나갔는지
	// relatedTarget 으로 직접 확인할 수 있어, 슬롯 사이를 지나갈 때 잘못 닫히지 않는다.
	function handlePointerOut(event) {
		if (!event.currentTarget.contains(event.relatedTarget)) close()
	}

	return (
		<div
			className={open ? 'disc is-open' : 'disc'}
			onPointerOver={() => setOpen(true)}
			onPointerOut={handlePointerOut}
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
			<div className="disc__zone" />

			{/* 화면 아래로 잘리는 원. 접혀 있을 때는 조금 더 가라앉는다. */}
			<div className="disc__surface" />

			<nav className="disc__slots" aria-label="프로젝트 전환">
				{slots.map((slot, index) => {
					// 감지 않는다. 감으면 끝의 로고가 반대편으로 순간이동해 디스크에 붙어 있지 않게 보인다.
					// 고른 것이 꼭대기로 오도록 원 전체가 돌 뿐, 로고끼리의 순서는 그대로다.
					const offset = index - selectedIndex
					const radian = (offset * STEP * Math.PI) / 180

					return (
						<button
							key={slot.to}
							type="button"
							className={offset === 0 ? 'disc__slot is-selected' : 'disc__slot'}
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
								{slot.home ? <House size={19} strokeWidth={2} /> : slot.name.slice(0, 1)}
							</span>
							<span className="disc__name">{slot.name}</span>
						</button>
					)
				})}
			</nav>
		</div>
	)
}
