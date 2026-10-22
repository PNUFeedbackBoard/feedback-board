import { useState } from 'react'
import codeplaceLogo from '../../assets/project-logos/codeplace.png'
import aipmsLogo from '../../assets/project-logos/aipms.png'
import aicapLogo from '../../assets/project-logos/aicap.png'
import aicmsLogo from '../../assets/project-logos/aicms.png'
import srvadmLogo from '../../assets/project-logos/srvadm.png'
import './project-logo.css'

const PROJECT_LOGOS = Object.freeze({
	codeplace: codeplaceLogo,
	aipms: aipmsLogo,
	aicap: aicapLogo,
	aicms: aicmsLogo,
	srvadm: srvadmLogo,
})

function projectLogoUrl(projectCode) {
	return PROJECT_LOGOS[projectCode] ?? null
}

export default function ProjectLogo({
	project,
	projectCode = project?.code,
	projectName = project?.name,
	className = '',
}) {
	const source = projectLogoUrl(projectCode) ?? project?.logoUrl ?? null
	const [failedSource, setFailedSource] = useState(null)
	const classes = ['project-logo', className].filter(Boolean).join(' ')

	if (source && source !== failedSource) {
		return (
			<span className={classes} aria-hidden="true">
				<img
					src={source}
					alt=""
					loading="lazy"
					decoding="async"
					onError={() => setFailedSource(source)}
				/>
			</span>
		)
	}

	return (
		<span className={`${classes} project-logo--fallback`} aria-hidden="true">
			{(projectName ?? projectCode ?? '?').slice(0, 1)}
		</span>
	)
}
