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
 * 원판의 반지름(px). **project-disc.css 의 --disc-radius 와 같은 값이어야 한다.**
 * 접히면 그 0.6배로 줄어든다. CSS 의 --disc-closed-scale 과 짝이다.
 */
const SURFACE_OPEN = 240
const CLOSED_SCALE = 0.46

/** 접혔을 때 로고 한 변의 길이(px). **CSS 의 --disc-logo-closed 와 같아야 한다.** */
const LOGO_CLOSED = 48
/**
 * 로고가 걸치는 비율. 0.2 면 위쪽 2할만 원 밖으로 나오고 8할이 면 위에 남는다.
 * 중심을 둘레선에 두면 반반이 되므로, 그보다 로고 높이의 (0.5 − 이 값)만큼 안쪽으로 들인다.
 */
const OVERHANG = 0.2

/**
 * 로고가 놓이는 반지름. 두 상태에서 뜻이 다르다.
 *
 *   펼침 — 원판 가장자리(240)보다 48 안쪽. 로고가 면 위에 온전히 앉는다.
 *   접힘 — 둘레선(110)에서 안쪽으로 들어간다. **위쪽 2할만 원 밖으로 나온다.**
 */
const RADIUS_OPEN = 192
const RADIUS_CLOSED = SURFACE_OPEN * CLOSED_SCALE - LOGO_CLOSED * (0.5 - OVERHANG)

/**
 * 원의 중심이 화면 아래 끝보다 얼마나 밑에 있는지.
 * 접힘 값은 작은 원을 깊이 묻지 않고 얕게 걸쳐 두어 곡률이 드러나게 잡았다.
 * 반지름에 비해 드러나는 높이가 클수록 호가 급하게 휜다 —
 * 지금은 반지름 110 에 높이 60, 너비 197 이라 눈에 띄게 둥근 봉우리가 된다.
 */
const DEPTH_OPEN = 60
const DEPTH_CLOSED = 46

/**
 * 슬롯 사이 각도. 호가 휘어 보이는 정도는 반지름이 아니라 이 각도 폭이 정한다.
 * (드롭 ÷ 가로폭 = tan(각도/2) 라 반지름은 약분된다. 반지름은 전체 크기만 바꾼다.)
 *
 * 원판이 작아 호를 따라 잰 간격도 좁다. 로고가 서로 닿지 않으려면 이만큼 벌려야 하고,
 * 그 대가로 세 칸 너머는 화면 아래로 돌아 나간다. 실제 다이얼도 그렇게 돈다.
 */
const STEP = 22

/**
 * 커서 각도를 회전으로 옮길 때의 비율. 1 이면 가리킨 로고가 정확히 꼭대기로 온다.
 * 절반쯤(0.55)으로 뒀더니 커서를 조금만 움직여도 원판이 크게 돌아 어지러웠다.
 * 0.3 이면 손을 따라 기우는 정도로만 반응한다.
 */
const TURN_GAIN = 0.3
/**
 * 회전 한계(도). 한 칸(STEP)보다 작게 두어, 마우스만으로는 옆 칸이 꼭대기로 넘어오지 않는다.
 * 칸을 옮기는 것은 클릭의 몫이고 이 기울임은 원판이 살아 있다는 신호일 뿐이다.
 */
const TURN_LIMIT = 16

export default function ProjectDisc({ projects, projectCode }) {
	const navigate = useNavigate()
	const [open, setOpen] = useState(false)
	const [turn, setTurn] = useState(0)

	/**
	 * 홈도 슬롯 하나다. 기획 6-1 의 "슬롯 하나는 홈 진입점으로 사용한다".
	 *
	 * **순서는 고정이다.** 로고는 디스크에 붙어 있고, 무엇을 고르든 서로의 좌우 관계가 바뀌지 않는다.
	 * 홈이 맨 왼쪽이고 프로젝트가 그 오른쪽으로 sortOrder 순서대로 늘어선다.
	 *
	 * 홈에 있는 동안에는 오른쪽 이웃만 보인다. 왼쪽에 아무것도 없기 때문이다.
	 * 끝쪽 프로젝트는 가까운 것을 한 번 거쳐 가야 닿는다.
	 */
	const slots = useMemo(
		() => [
			{ key: '', name: '전체 현황', to: '/admin', home: true },
			...projects.map((project) => ({
				key: project.code,
				name: project.name,
				to: `/admin/${project.code}/board`,
			})),
		],
		[projects],
	)

	const selectedIndex = Math.max(
		0,
		slots.findIndex((slot) => slot.key === (projectCode ?? '')),
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
	 * 커서가 원 중심에서 몇 도에 있는지를 재어 그만큼 원판을 돌린다.
	 *
	 * 어느 요소 위에 있는지가 아니라 **좌표만** 보기 때문에, 돌아간 결과가 다시 입력이 되는
	 * 되먹임이 없다. 커서를 세워 두면 원판도 그 자리에 멈춰 선다.
	 */
	function handlePointerMove(event) {
		if (!open) return
		const bounds = event.currentTarget.getBoundingClientRect()
		const centerX = bounds.left + bounds.width / 2
		const centerY = bounds.bottom + DEPTH_OPEN
		const degrees =
			(Math.atan2(event.clientX - centerX, centerY - event.clientY) * 180) / Math.PI
		const next = Math.max(-TURN_LIMIT, Math.min(TURN_LIMIT, -degrees * TURN_GAIN))
		// 1도 단위로 끊어 커서가 미세하게 떨릴 때마다 다시 그리지 않게 한다.
		setTurn(Math.round(next))
	}

	// 두 상태의 기하가 다르다. 접히면 원이 0.6배로 줄고 로고는 그 둘레선에 얹힌다.
	const radius = open ? RADIUS_OPEN : RADIUS_CLOSED
	const depth = open ? DEPTH_OPEN : DEPTH_CLOSED

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
			<div className="disc__zone" />

			{/* 화면 아래로 잘리는 원. 접혀 있을 때는 조금 더 가라앉는다. */}
			<div className="disc__surface" />

			<nav className="disc__slots" aria-label="프로젝트 전환">
				{slots.map((slot, index) => {
					// 감지 않는다. 감으면 끝의 로고가 반대편으로 순간이동해 디스크에 붙어 있지 않게 보인다.
					// 고른 것이 꼭대기로 오도록 원 전체가 돌 뿐, 로고끼리의 순서는 그대로다.
					const offset = index - selectedIndex
					const radian = ((offset * STEP + turn) * Math.PI) / 180

					return (
						<button
							key={slot.to}
							type="button"
							className={offset === 0 ? 'disc__slot is-selected' : 'disc__slot'}
							style={{
								// 원의 중심은 화면 아래 끝 가운데에서 depth 만큼 더 내려간 곳이다.
								transform: `translate(-50%, 50%) translate(${radius * Math.sin(radian)}px, ${-(radius * Math.cos(radian) - depth)}px)`,
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
