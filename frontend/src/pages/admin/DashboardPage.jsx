import { useEffect, useState } from 'react'
import {
	Bar,
	BarChart,
	CartesianGrid,
	Cell,
	Line,
	LineChart,
	Pie,
	PieChart,
	ResponsiveContainer,
	Tooltip,
	XAxis,
	YAxis,
} from 'recharts'
import { getDashboard } from '../../api/endpoints.js'
import {
	BOARD_COLUMN_STATUSES,
	FEEDBACK_CATEGORY,
	FEEDBACK_STATUS,
	labelOf,
} from '../../constants/enums.js'
import StatusBadge from './components/StatusBadge.jsx'
import './dashboard.css'

/**
 * 홈 — 전체 현황 대시보드. 기획 6-2
 *
 * **조회 전용이다.** 여기서는 상태를 바꾸거나 답변을 쓰지 않는다.
 * 지표 6종을 기획의 표 순서대로 담는다.
 *
 * 차트 색은 admin-tokens.temp.css 에 있다. 배지의 옅은 틴트와 달리 면을 채우는 색이라
 * 따로 두었고, 색각 이상 검증을 통과한 값이다.
 *
 * TODO(C, 5단계): B 의 집계 쿼리가 정리되면(피드백 1,000건 기준) 응답 시간을 다시 확인한다.
 */

/** 누적 막대에 쌓는 순서. 진행 순서대로 쌓아야 막대를 왼쪽부터 읽을 수 있다. */
const STACK_ORDER = [...BOARD_COLUMN_STATUSES, 'REJECTED']

const CATEGORY_COLOR = {
	BUG: 'var(--color-chart-category-bug)',
	FEATURE: 'var(--color-chart-category-feature)',
	ETC: 'var(--color-chart-category-etc)',
}

const STATUS_COLOR = {
	RECEIVED: 'var(--color-chart-received)',
	IN_PROGRESS: 'var(--color-chart-in-progress)',
	DONE: 'var(--color-chart-done)',
	REJECTED: 'var(--color-chart-rejected)',
}

export default function DashboardPage() {
	const [data, setData] = useState(null)
	const [message, setMessage] = useState('')

	useEffect(() => {
		let cancelled = false

		getDashboard()
			.then((found) => !cancelled && setData(found))
			.catch((error) => !cancelled && setMessage(error.message))

		return () => {
			cancelled = true
		}
	}, [])

	if (message) return <p className="board__notice">{message}</p>
	if (!data) return <p className="board__notice">불러오는 중…</p>

	const total = STACK_ORDER.reduce((sum, status) => sum + (data.statusCounts[status] ?? 0), 0)

	// 프로젝트별 교차 집계를 막대 한 칸씩으로 편다.
	const projectRows = data.projectStatusCounts.map((row) => ({
		name: row.projectName,
		...Object.fromEntries(STACK_ORDER.map((status) => [status, row.counts[status] ?? 0])),
	}))

	return (
		<div className="dash">
			{/* ── 상태별 건수 ─────────────────────────────────────── */}
			<section className="dash__tiles">
				{STACK_ORDER.map((status) => (
					<article key={status} className="tile">
						<StatusBadge status={status} />
						<p className="tile__figure">
							<strong className="tile__value">{data.statusCounts[status] ?? 0}</strong>
							<span className="tile__unit">건</span>
						</p>
					</article>
				))}
				<article className="tile">
					<span className="tile__label">평균 처리 소요</span>
					<p className="tile__figure">
						<strong className="tile__value">{formatHours(data.averageProcessingHours)}</strong>
					</p>
					<span className="tile__note">완료·반영 불가 기준</span>
				</article>
				<article className="tile">
					<span className="tile__label">이용자</span>
					<p className="tile__figure">
						<strong className="tile__value">{data.userCount.member + data.userCount.guest}</strong>
						<span className="tile__unit">명</span>
					</p>
					<span className="tile__note">
						회원 {data.userCount.member} · 비회원 {data.userCount.guest}
					</span>
				</article>
			</section>

			{/* ── 차트 ────────────────────────────────────────────── */}
			<section className="dash__charts">
				<Panel title="프로젝트별 현황" caption="상태를 쌓아 올린 막대입니다">
					<Legend items={STACK_ORDER.map((s) => ({ key: s, label: labelOf(FEEDBACK_STATUS, s), color: STATUS_COLOR[s] }))} />
					<ResponsiveContainer width="100%" height={240}>
						<BarChart data={projectRows} margin={{ top: 4, right: 4, bottom: 0, left: -18 }}>
							<CartesianGrid vertical={false} stroke="var(--color-chart-grid)" />
							<XAxis dataKey="name" tickLine={false} axisLine={false} tick={AXIS_TICK} interval={0} />
							<YAxis tickLine={false} axisLine={false} tick={AXIS_TICK} allowDecimals={false} />
							<Tooltip content={<ChartTooltip labels={FEEDBACK_STATUS} unit="건" />} cursor={{ fill: 'var(--color-surface-sunken)' }} />
							{STACK_ORDER.map((status, index) => (
								<Bar
									key={status}
									dataKey={status}
									stackId="status"
									fill={STATUS_COLOR[status]}
									// 배경색으로 얇게 둘러 조각 사이에 틈을 만든다. 붙어 있으면 경계가 안 읽힌다.
									stroke="var(--color-surface)"
									strokeWidth={2}
									// 맨 위 조각만 둥글게. 중간까지 둥글면 쌓인 것이 어긋나 보인다.
									radius={index === STACK_ORDER.length - 1 ? [4, 4, 0, 0] : 0}
								/>
							))}
						</BarChart>
					</ResponsiveContainer>
				</Panel>

				<Panel title="유형별 비율" caption={`전체 ${total}건 기준`}>
					<div className="dash__donut">
						<ResponsiveContainer width="100%" height={200}>
							<PieChart>
								<Pie
									data={data.categoryCounts}
									dataKey="count"
									nameKey="category"
									innerRadius={52}
									outerRadius={80}
									paddingAngle={2}
									stroke="var(--color-surface)"
									strokeWidth={2}
								>
									{data.categoryCounts.map((slice) => (
										<Cell key={slice.category} fill={CATEGORY_COLOR[slice.category]} />
									))}
								</Pie>
								<Tooltip content={<ChartTooltip labels={FEEDBACK_CATEGORY} unit="건" />} />
							</PieChart>
						</ResponsiveContainer>

						{/*
						 * 조각 옆에 건수와 비율을 글자로 같이 둔다.
						 * 유형 색 가운데 둘은 흰 배경에서 명암이 3:1 에 못 미쳐 색만으로 읽히지 않는다.
						 */}
						<ul className="dash__breakdown">
							{data.categoryCounts.map((slice) => (
								<li key={slice.category}>
									<span className="dash__swatch" style={{ background: CATEGORY_COLOR[slice.category] }} />
									<span className="dash__breakdown-name">{labelOf(FEEDBACK_CATEGORY, slice.category)}</span>
									<span className="dash__breakdown-value">
										{slice.count}건 · {Math.round(slice.ratio * 100)}%
									</span>
								</li>
							))}
						</ul>
					</div>
				</Panel>

				<Panel title="기간별 추이" caption="최근 30일 접수 건수" wide>
					<ResponsiveContainer width="100%" height={200}>
						<LineChart data={data.dailyTrend} margin={{ top: 8, right: 8, bottom: 0, left: -18 }}>
							<CartesianGrid vertical={false} stroke="var(--color-chart-grid)" />
							<XAxis dataKey="date" tickLine={false} axisLine={false} tick={AXIS_TICK} tickFormatter={shortDay} minTickGap={24} />
							<YAxis tickLine={false} axisLine={false} tick={AXIS_TICK} allowDecimals={false} />
							<Tooltip content={<ChartTooltip unit="건" />} cursor={{ stroke: 'var(--color-chart-axis)' }} />
							{/* 시리즈가 하나뿐이라 범례를 두지 않는다. 제목이 이름을 대신한다. */}
							<Line
								type="monotone"
								dataKey="count"
								stroke="var(--color-chart-received)"
								strokeWidth={2}
								dot={false}
								activeDot={{ r: 4 }}
							/>
						</LineChart>
					</ResponsiveContainer>
				</Panel>
			</section>

			{/* ── 목록 ────────────────────────────────────────────── */}
			<section className="dash__lists">
				<Panel title="우선 처리 요청" caption={`${data.priorityRequested.length}건`}>
					<FeedbackRows items={data.priorityRequested} empty="요청된 항목이 없습니다." />
				</Panel>
				<Panel title="최근 접수" caption="10건">
					<FeedbackRows items={data.recent} empty="접수된 항목이 없습니다." />
				</Panel>
			</section>
		</div>
	)
}

/** 축 눈금의 글자. 데이터보다 뒤로 물러나야 한다. */
const AXIS_TICK = { fill: 'var(--color-text-subtle)', fontSize: 11 }

function Panel({ title, caption, wide = false, children }) {
	return (
		<section className={wide ? 'panel-card panel-card--wide' : 'panel-card'}>
			<header className="panel-card__head">
				<h2 className="panel-card__title">{title}</h2>
				{caption && <span className="panel-card__caption">{caption}</span>}
			</header>
			{children}
		</section>
	)
}

/** 색만으로 구분되지 않도록 이름을 붙여 둔다. */
function Legend({ items }) {
	return (
		<ul className="dash__legend">
			{items.map((item) => (
				<li key={item.key}>
					<span className="dash__swatch" style={{ background: item.color }} />
					{item.label}
				</li>
			))}
		</ul>
	)
}

/** 차트 위에 올리는 설명 상자. 한국어 이름으로 바꿔 보여 준다. */
function ChartTooltip({ active, payload, label, labels, unit }) {
	if (!active || !payload?.length) return null

	return (
		<div className="dash__tip">
			<p className="dash__tip-head">{labels ? label : shortDay(label)}</p>
			{payload.map((row) => (
				<p key={row.dataKey} className="dash__tip-row">
					<span className="dash__swatch" style={{ background: row.color }} />
					{labels ? labelOf(labels, row.name ?? row.dataKey) : '접수'}
					<strong>
						{row.value}
						{unit}
					</strong>
				</p>
			))}
		</div>
	)
}

function FeedbackRows({ items, empty }) {
	if (items.length === 0) return <p className="board__empty">{empty}</p>

	return (
		<ul className="dash__rows">
			{items.map((item) => (
				<li key={item.id} className="dash__row">
					<StatusBadge status={item.status} />
					<span className="dash__row-title">{item.title}</span>
					<span className="dash__row-meta">{item.projectName}</span>
				</li>
			))}
		</ul>
	)
}

/** 'YYYY-MM-DD' 를 'M/D' 로 줄인다. 30일치를 늘어놓으면 전체 날짜는 겹친다. */
function shortDay(date) {
	const [, month, day] = String(date).split('-')
	return `${Number(month)}/${Number(day)}`
}

/** 시간이 24를 넘으면 일 단위로 바꿔 읽기 쉽게 한다. */
function formatHours(hours) {
	if (hours >= 24) return `${(hours / 24).toFixed(1)}일`
	return `${hours.toFixed(1)}시간`
}
