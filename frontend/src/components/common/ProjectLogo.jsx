import './project-logo.css'

const PROJECT_GLYPHS = {
	codeplace: (
		<>
			<path d="m9 6.5-5 5.5 5 5.5" />
			<path d="m15 6.5 5 5.5-5 5.5" />
			<path d="m13.5 4.5-3 15" />
		</>
	),
	aipms: (
		<>
			<rect x="3.5" y="5.5" width="17" height="15" rx="2" />
			<path d="M8 3.5v4M16 3.5v4M3.5 9.5h17" />
			<path d="m8 15 2 2 5-5" />
		</>
	),
	aicap: (
		<>
			<path d="m3 8.5 9-4.5 9 4.5-9 4.5z" />
			<path d="M7 11v4.5c2.8 2.4 7.2 2.4 10 0V11" />
			<path d="M21 8.5V14" />
		</>
	),
	aicms: (
		<>
			<circle cx="8" cy="8" r="3" />
			<circle cx="16" cy="8" r="3" />
			<path d="M2.8 19c.5-3.8 2.7-6 5.2-6s4.7 2.2 5.2 6" />
			<path d="M10.8 19c.5-3.8 2.7-6 5.2-6s4.7 2.2 5.2 6" />
		</>
	),
	srvadm: (
		<>
			<rect x="3.5" y="4" width="17" height="6.5" rx="1.5" />
			<rect x="3.5" y="13.5" width="17" height="6.5" rx="1.5" />
			<path d="M10 7.25h6.5M10 16.75h6.5" />
			<circle cx="7" cy="7.25" r="0.7" fill="currentColor" stroke="none" />
			<circle cx="7" cy="16.75" r="0.7" fill="currentColor" stroke="none" />
		</>
	),
}

export default function ProjectLogo({
	project,
	projectCode = project?.code,
	projectName = project?.name,
	className = '',
}) {
	const glyph = PROJECT_GLYPHS[projectCode]
	const classes = ['project-logo', className].filter(Boolean).join(' ')

	if (glyph) {
		return (
			<span className={classes} aria-hidden="true">
				<svg
					className="project-logo__glyph"
					data-project-logo={projectCode}
					viewBox="0 0 24 24"
					fill="none"
					stroke="currentColor"
					strokeWidth="1.6"
					strokeLinecap="round"
					strokeLinejoin="round"
				>
					{glyph}
				</svg>
			</span>
		)
	}

	return (
		<span className={`${classes} project-logo--fallback`} aria-hidden="true">
			{(projectName ?? projectCode ?? '?').slice(0, 1)}
		</span>
	)
}
